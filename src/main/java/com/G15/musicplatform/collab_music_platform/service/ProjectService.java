package com.G15.musicplatform.collab_music_platform.service;

import com.G15.musicplatform.collab_music_platform.model.Project;
import com.G15.musicplatform.collab_music_platform.model.ProjectFile;
import com.G15.musicplatform.collab_music_platform.model.User;
import com.G15.musicplatform.collab_music_platform.repository.ProjectFileRepository;
import com.G15.musicplatform.collab_music_platform.repository.ProjectRepository;
import com.G15.musicplatform.collab_music_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectFileRepository projectFileRepository;

    @Value("${upload.path}")
    private String uploadDir;

    /**
     * Allows any user to create a project (no prior role needed).
     * Immediately afterward, that user is assigned the PROJECT_OWNER role for that project.
     */
    public Project createProject(String name, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 1: Create and save the project
        Project project = new Project();
        project.setName(name);
        project.setUser(user);  // The "owning user" field if you still want to track who 'created' it
        Project savedProject = projectRepository.save(project);

        // Step 2: Now assign the user the PROJECT_OWNER role for this project
        savedProject.getUserRoles().put(user, "PROJECT_OWNER");
        savedProject = projectRepository.save(savedProject);

        return savedProject;
    }

    public List<Project> getAllProjectsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // If you only want to show projects that the user is "involved in" (role or owner),
        // you could do a more custom query. But right now, this just returns projects by .getUser() match.
        return projectRepository.findByUser(user);
    }

    /**
     * For assigning a role, only a PROJECT_OWNER (of that project) can do it.
     */
    public void assignRole(Long projectId, Long targetUserId, String newRole, String requestingUsername) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User requestingUser = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new RuntimeException("Requesting user not found"));
        String requestingUserRole = project.getUserRoles().getOrDefault(requestingUser, "NONE");

        // Only PROJECT_OWNER can assign roles
        if (!"PROJECT_OWNER".equals(requestingUserRole)) {
            throw new RuntimeException("Only the project owner can assign roles.");
        }

        // Now assign the role to the target user
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        project.getUserRoles().put(targetUser, newRole);
        projectRepository.save(project);
    }

    /**
     * For revoking a role, only a PROJECT_OWNER can do it.
     */
    public void revokeRole(Long projectId, Long targetUserId, String requestingUsername) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User requestingUser = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new RuntimeException("Requesting user not found"));
        String requestingUserRole = project.getUserRoles().getOrDefault(requestingUser, "NONE");

        // Only PROJECT_OWNER can revoke roles
        if (!"PROJECT_OWNER".equals(requestingUserRole)) {
            throw new RuntimeException("Only the project owner can revoke roles.");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        project.getUserRoles().remove(targetUser);
        projectRepository.save(project);
    }

    public String getRoleForUser(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return project.getUserRoles().getOrDefault(user, "NONE");
    }

    /**
     * Deletes an entire project. Only the PROJECT_OWNER can do this.
     */
    public void deleteProject(Long projectId, String requestingUsername) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User requestingUser = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new RuntimeException("Requesting user not found"));
        String requestingUserRole = project.getUserRoles().getOrDefault(requestingUser, "NONE");

        if (!"PROJECT_OWNER".equals(requestingUserRole)) {
            throw new RuntimeException("Only the project owner can delete this project.");
        }

        // Delete files from the filesystem first
        for (ProjectFile file : project.getProjectFiles()) {
            deleteFileFromSystem(file.getFilePath());
        }

        projectFileRepository.deleteAll(project.getProjectFiles());

        // Finally, delete the project
        projectRepository.delete(project);
    }

    /**
     * Deletes a file from a project. Owners and Contributors can do this; Reviewers cannot.
     */
    public void deleteFileFromProject(Long projectId, Long fileId, String requestingUsername) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Check the requestor's role
        User requestingUser = userRepository.findByUsername(requestingUsername)
                .orElseThrow(() -> new RuntimeException("Requesting user not found"));
        String requestingUserRole = project.getUserRoles().getOrDefault(requestingUser, "NONE");

        // Only owners and contributors can delete files
        if (!("PROJECT_OWNER".equals(requestingUserRole) || "CONTRIBUTOR".equals(requestingUserRole))) {
            throw new RuntimeException("You do not have permission to delete files in this project.");
        }

        ProjectFile file = projectFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Check if file belongs to the given project
        if (!file.getProject().getId().equals(projectId)) {
            throw new RuntimeException("File does not belong to the specified project.");
        }

        // Delete from filesystem
        deleteFileFromSystem(file.getFilePath());

        // Delete record from DB
        projectFileRepository.delete(file);
    }

    private void deleteFileFromSystem(String filePath) {
        if (filePath != null) {
            File fileOnDisk = new File(filePath);
            if (fileOnDisk.exists()) {
                fileOnDisk.delete();
            }
        }
    }

    // In ProjectService.java
    public String getRoleForUserByUsername(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return the role from projectRoles map or "NONE" if missing
        return project.getUserRoles().getOrDefault(user, "NONE");
    }

}
