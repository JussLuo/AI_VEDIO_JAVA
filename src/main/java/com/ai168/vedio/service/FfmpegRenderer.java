package com.ai168.vedio.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FfmpegRenderer {
    private final String executable;

    public FfmpegRenderer(@Value("${vedio.ffmpeg-path:ffmpeg}") String executable) {
        this.executable = executable;
    }

    public void render(List<Path> clips, Path output) throws IOException, InterruptedException {
        if (clips.isEmpty() || clips.size() > 30) {
            throw new IOException("合成片段数量无效");
        }
        Files.createDirectories(output.getParent());
        Path workDirectory = Files.createTempDirectory(output.getParent(), "render-");
        try {
            List<Path> normalized = new ArrayList<>();
            for (int index = 0; index < clips.size(); index++) {
                Path clip = clips.get(index);
                if (!Files.isRegularFile(clip)) {
                    throw new IOException("视频片段不存在");
                }
                Path normalizedClip = workDirectory.resolve(index + ".mp4");
                normalize(clip, normalizedClip, workDirectory);
                normalized.add(normalizedClip);
            }
            Path concatList = workDirectory.resolve("clips.txt");
            List<String> lines = new ArrayList<>();
            for (Path clip : normalized) {
                String path = clip.toAbsolutePath().toString()
                        .replace('\\', '/')
                        .replace("'", "'\\''");
                lines.add("file '" + path + "'");
            }
            Files.write(concatList, lines, StandardCharsets.UTF_8);
            Path temporaryOutput = workDirectory.resolve("output.mp4");
            execute(List.of(
                    executable, "-hide_banner", "-nostdin", "-y",
                    "-f", "concat", "-safe", "0", "-i", concatList.toString(),
                    "-c", "copy", "-movflags", "+faststart", temporaryOutput.toString()),
                    workDirectory, Duration.ofMinutes(10));
            Files.move(temporaryOutput, output, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            try (var files = Files.walk(workDirectory)) {
                files.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    private void normalize(Path input, Path output, Path workDirectory) throws IOException, InterruptedException {
        boolean hasAudio = hasAudio(input);
        List<String> command = new ArrayList<>(List.of(
                executable, "-hide_banner", "-nostdin", "-y", "-i", input.toString()));
        if (!hasAudio) {
            command.addAll(List.of("-f", "lavfi", "-i", "anullsrc=channel_layout=stereo:sample_rate=44100"));
        }
        command.addAll(List.of(
                "-map", "0:v:0", "-map", hasAudio ? "0:a:0" : "1:a:0",
                "-vf", "scale=854:480:force_original_aspect_ratio=decrease,"
                        + "pad=854:480:(ow-iw)/2:(oh-ih)/2,setsar=1,fps=24",
                "-af", "apad", "-c:v", "libx264", "-preset", "veryfast",
                "-pix_fmt", "yuv420p", "-c:a", "aac", "-ar", "44100",
                "-ac", "2", "-shortest", output.toString()));
        execute(command, workDirectory, Duration.ofMinutes(10));
    }

    private boolean hasAudio(Path input) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(executable, "-hide_banner", "-i", input.toString())
                .redirectErrorStream(true)
                .start();
        String output;
        try (var stream = process.getInputStream()) {
            output = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        process.waitFor();
        if (!output.contains("Video:")) {
            throw new IOException("片段不含视频流");
        }
        return output.contains("Audio:");
    }

    private void execute(List<String> command, Path workDirectory, Duration timeout)
            throws IOException, InterruptedException {
        Path log = workDirectory.resolve("ffmpeg.log");
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .start();
        if (!process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IOException("FFmpeg 合成超时");
        }
        if (process.exitValue() != 0) {
            throw new IOException("FFmpeg 合成失败");
        }
    }
}
