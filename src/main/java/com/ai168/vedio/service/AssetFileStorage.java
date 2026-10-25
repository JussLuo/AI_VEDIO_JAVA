package com.ai168.vedio.service;

import cn.hutool.core.util.IdUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetFileStorage {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private final Path root;

    public AssetFileStorage(@Value("${vedio.upload-dir:./data/uploads}") String uploadDirectory) {
        this.root = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    public StoredFile save(String projectId, MultipartFile file) {
        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片为空或超过 10 MiB");
        }
        String mimeType = detectImageType(file);
        String extension = switch (mimeType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            default -> ".webp";
        };
        Path relativePath = Paths.get("projects", projectId, IdUtil.fastSimpleUUID() + extension);
        Path destination = resolve(relativePath);
        try {
            Files.createDirectories(destination.getParent());
            file.transferTo(destination);
            return new StoredFile(relativePath.toString().replace('\\', '/'), mimeType, file.getSize());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "保存图片失败", exception);
        }
    }

    public byte[] read(String projectId, String filePath) {
        Path path = uploadedPath(projectId, filePath);
        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "素材文件不存在");
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "读取图片失败", exception);
        }
    }

    public void delete(String projectId, String filePath) {
        try {
            Files.deleteIfExists(uploadedPath(projectId, filePath));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "删除图片失败", exception);
        }
    }

    public boolean isManaged(String projectId, String filePath) {
        return filePath != null && filePath.startsWith("projects/" + projectId + "/");
    }

    private Path uploadedPath(String projectId, String filePath) {
        if (!isManaged(projectId, filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "素材文件不存在");
        }
        Path relativePath = Paths.get(filePath).normalize();
        if (relativePath.getNameCount() != 3
                || !relativePath.getName(0).toString().equals("projects")
                || !relativePath.getName(1).toString().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "素材文件不存在");
        }
        return resolve(relativePath);
    }

    private Path resolve(Path relativePath) {
        Path path = root.resolve(relativePath).normalize();
        if (!path.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效素材路径");
        }
        return path;
    }

    private String detectImageType(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = Arrays.copyOf(inputStream.readNBytes(12), 12);
            if (header[0] == (byte) 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
                    && header[4] == 0x0D && header[5] == 0x0A && header[6] == 0x1A && header[7] == 0x0A) {
                return "image/png";
            }
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
                return "image/jpeg";
            }
            if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
                return "image/webp";
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无法读取图片", exception);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅支持 PNG、JPEG、WebP 图片");
    }

    public record StoredFile(String relativePath, String mimeType, long size) {
    }
}
