package com.G15.musicplatform.collab_music_platform.controller;

import com.G15.musicplatform.collab_music_platform.model.Project;
import com.G15.musicplatform.collab_music_platform.model.ProjectFile;
import com.G15.musicplatform.collab_music_platform.model.User;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Create a project. The user is later assigned as PROJECT_OWNER in the service layer.
     */
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

    /**
     * Upload file: owners & contributors may do so (if you want a role check, add one in service).
     * For simplicity, we won't do a check here, but you can easily add one if needed.
     */
    @PostMapping("/{projectId}/upload")
    public ResponseEntity<String> uploadFileToProject(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("username") String username) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // If you want to enforce role check for upload, do it in ProjectService.
        // E.g., projectService.uploadFile(...)

        try {
            // Save file physically
            String filePath = uploadDir + "/" + file.getOriginalFilename();
            File destination = new File(filePath);
            if (!destination.getParentFile().exists()) {
                destination.getParentFile().mkdirs();
            }
            file.transferTo(destination);

            // Save metadata
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

    /**
     * Delete an entire project, only allowed for PROJECT_OWNER
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(
            @PathVariable Long projectId,
            @RequestParam("username") String username) {
        projectService.deleteProject(projectId, username);
        return ResponseEntity.ok("Project deleted successfully.");
    }

    /**
     * Delete a file within a project, allowed for PROJECT_OWNER and CONTRIBUTOR
     */
    @DeleteMapping("/{projectId}/files/{fileId}")
    public ResponseEntity<String> deleteFileFromProject(
            @PathVariable Long projectId,
            @PathVariable Long fileId,
            @RequestParam("username") String username) {
        projectService.deleteFileFromProject(projectId, fileId, username);
        return ResponseEntity.ok("File deleted successfully.");
    }

    /**
     * Assign a role to another user. Only PROJECT_OWNER can do so.
     * e.g. /api/projects/5/assign-role?userId=10&role=CONTRIBUTOR&username=alice
     */
    @PostMapping("/{projectId}/assign-role")
    public ResponseEntity<String> assignRole(
            @PathVariable Long projectId,
            @RequestParam Long userId,
            @RequestParam String role,
            @RequestParam("username") String requestingUser) {
        projectService.assignRole(projectId, userId, role, requestingUser);
        return ResponseEntity.ok("Role assigned successfully.");
    }

    /**
     * Revoke an existing role from a user. Only PROJECT_OWNER can do so.
     */
    @PostMapping("/{projectId}/revoke-role")
    public ResponseEntity<String> revokeRole(
            @PathVariable Long projectId,
            @RequestParam Long userId,
            @RequestParam("username") String requestingUser) {
        projectService.revokeRole(projectId, userId, requestingUser);
        return ResponseEntity.ok("Role revoked successfully.");
    }

    /**
     * Get the role for a particular user in a project.
     */
    @GetMapping("/{projectId}/role")
    public ResponseEntity<String> getRoleForUser(
            @PathVariable Long projectId,
            @RequestParam String username) {
        String role = projectService.getRoleForUserByUsername(projectId, username);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/{projectId}/users")
    public List<Map<String, String>> getProjectUsers(@PathVariable Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        // Build a simple list of { username: "bob", role: "CONTRIBUTOR" } objects
        List<Map<String, String>> userList = new ArrayList<>();
        for (Map.Entry<User, String> entry : project.getUserRoles().entrySet()) {
            Map<String, String> u = new HashMap<>();
            u.put("username", entry.getKey().getUsername());
            u.put("role", entry.getValue());
            userList.add(u);
        }
        return userList;
    }

}
