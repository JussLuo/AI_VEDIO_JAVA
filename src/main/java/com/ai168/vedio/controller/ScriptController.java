package com.ai168.vedio.controller;

import com.ai168.vedio.dto.CreateScriptRequest;
import com.ai168.vedio.dto.UpdateScriptRequest;
import com.ai168.vedio.entity.Script;
import com.ai168.vedio.service.ScriptService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScriptController {
    private final ScriptService scriptService;

    public ScriptController(ScriptService scriptService) {
        this.scriptService = scriptService;
    }
    @PostMapping("/api/projects/{projectId}/scripts")
    public ResponseEntity<Script> createScript(@PathVariable String projectId, @Valid @RequestBody CreateScriptRequest request) {
        Script script = scriptService.createScript(projectId, request);
        return ResponseEntity.created(URI.create("/api/scripts/" + script.getId())).body(script);
    }
    @GetMapping("/api/projects/{projectId}/scripts")
    public List<Script> listProjectScripts(@PathVariable String projectId) {
        return scriptService.listProjectScripts(projectId);
    }
    @GetMapping("/api/scripts/{id}")
    public ResponseEntity<Script> getScript(@PathVariable String id) {
        return scriptService.findScriptById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PutMapping("/api/scripts/{id}")
    public ResponseEntity<Script> updateScript(@PathVariable String id, @Valid @RequestBody UpdateScriptRequest request) {
        return scriptService.updateScript(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @DeleteMapping("/api/scripts/{id}")
    public ResponseEntity<Void> deleteScript(@PathVariable String id) {
        if (scriptService.deleteScript(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
