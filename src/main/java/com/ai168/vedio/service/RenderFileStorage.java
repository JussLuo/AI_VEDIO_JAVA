package com.ai168.vedio.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RenderFileStorage {
    private final Path root;

    public RenderFileStorage(@Value("${vedio.render-dir:./data/renders}") String renderDirectory) {
        this.root = Paths.get(renderDirectory).toAbsolutePath().normalize();
    }

    public Path target(String taskId) {
        if (taskId == null || !taskId.matches("[a-fA-F0-9]{32}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效合成任务标识");
        }
        return root.resolve(taskId + ".mp4");
    }

    public byte[] read(String fileName) {
        if (fileName == null || !fileName.matches("[a-fA-F0-9]{32}\\.mp4")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "成片不存在");
        }
        try {
            return Files.readAllBytes(root.resolve(fileName));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "成片不存在");
        }
    }
}
