package com.ai168.vedio.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VideoFileStorage {
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024;
    private final Path root;
    private final HttpClient httpClient;

    public VideoFileStorage(@Value("${vedio.video-dir:./data/videos}") String videoDirectory) {
        this.root = Paths.get(videoDirectory).toAbsolutePath().normalize();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    public String save(String taskId, String outputUrl) {
        if (taskId == null || !taskId.matches("[a-fA-F0-9]{32}")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "无效任务标识");
        }
        URI uri;
        try {
            uri = URI.create(outputUrl);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "视频下载地址无效");
        }
        String host = uri.getHost();
        if (!"https".equals(uri.getScheme()) || host == null
                || !(host.equals("replicate.delivery") || host.endsWith(".replicate.delivery"))) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "视频下载地址无效");
        }
        Path target = root.resolve(taskId + ".mp4");
        Path temporary = root.resolve(taskId + ".part");
        try {
            Files.createDirectories(root);
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMinutes(5))
                    .GET()
                    .build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                response.body().close();
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "视频下载失败");
            }
            try (InputStream input = response.body(); OutputStream output = Files.newOutputStream(temporary)) {
                byte[] buffer = new byte[8192];
                long total = 0;
                int count;
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (total > MAX_VIDEO_SIZE) {
                        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "视频文件过大");
                    }
                    output.write(buffer, 0, count);
                }
            }
            Files.move(temporary, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return target.getFileName().toString();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "保存视频失败");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "下载视频已中断");
        } finally {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
            }
        }
    }

    public byte[] read(String fileName) {
        try {
            return Files.readAllBytes(pathFor(fileName));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "视频不存在");
        }
    }

    public Path pathFor(String fileName) {
        if (fileName == null || !fileName.matches("[a-fA-F0-9]{32}\\.mp4")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "视频不存在");
        }
        Path path = root.resolve(fileName);
        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "视频不存在");
        }
        return path;
    }
}
