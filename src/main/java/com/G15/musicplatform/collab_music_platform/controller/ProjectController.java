package com.G15.musicplatform.collab_music_platform.controller;

import com.G15.musicplatform.collab_music_platform.model.Project;
import com.G15.musicplatform.collab_music_platform.model.ProjectFile;
import com.G15.musicplatform.collab_music_platform.repository.ProjectFileRepository;
import com.G15.musicplatform.collab_music_platform.repository.ProjectRepository;
import com.G15.musicplatform.collab_music_platform.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectFileRepository projectFileRepository;

    @Value("${upload.path}")
    private String uploadDir;

    @PostMapping
    public ResponseEntity<String> createProject(
            @RequestParam("name") String name,
            @RequestParam("username") String username) {
        Project project = projectService.createProject(name, username);
        return ResponseEntity.ok("Project created: " + project.getName());
    }

    @GetMapping
    public ResponseEntity<List<Project>> listProjects(@RequestParam("username") String username) {
        List<Project> projects = projectService.getAllProjectsForUser(username);
        return ResponseEntity.ok(projects);
    }

    @PostMapping("/{projectId}/upload")
    public ResponseEntity<String> uploadFileToProject(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        try {
            // Save file to upload directory
            String filePath = uploadDir + "/" + file.getOriginalFilename();
            File destination = new File(filePath);
            if (!destination.getParentFile().exists()) {
                destination.getParentFile().mkdirs();
            }
            file.transferTo(destination);

            // Save file metadata
            ProjectFile projectFile = new ProjectFile();
            projectFile.setFileName(file.getOriginalFilename());
            projectFile.setFilePath(filePath);
            projectFile.setProject(project);

            projectFileRepository.save(projectFile);

            return ResponseEntity.ok("File uploaded to project: " + project.getName());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/{projectId}/files")
    public ResponseEntity<List<ProjectFile>> getProjectFiles(@PathVariable Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return ResponseEntity.ok(project.getProjectFiles());
    }

    // NEW: Delete Project Endpoint
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok("Project deleted successfully.");
    }

    // NEW: Delete File Endpoint
    @DeleteMapping("/{projectId}/files/{fileId}")
    public ResponseEntity<String> deleteFileFromProject(
            @PathVariable Long projectId,
            @PathVariable Long fileId) {
        projectService.deleteFileFromProject(projectId, fileId);
        return ResponseEntity.ok("File deleted successfully.");
    }

}
