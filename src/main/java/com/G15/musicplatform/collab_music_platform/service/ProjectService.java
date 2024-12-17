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

    public Project createProject(String name, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = new Project();
        project.setName(name);
        project.setUser(user);

        return projectRepository.save(project);
    }

    public List<Project> getAllProjectsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return projectRepository.findByUser(user);
    }

    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Delete files from the filesystem
        for (ProjectFile file : project.getProjectFiles()) {
            deleteFileFromSystem(file.getFilePath());
        }

        // The cascade might handle this, but to be explicit:
        projectFileRepository.deleteAll(project.getProjectFiles());

        // Delete the project
        projectRepository.delete(project);
    }

    public void deleteFileFromProject(Long projectId, Long fileId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        ProjectFile file = projectFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Check if file belongs to the given project
        if (!file.getProject().getId().equals(projectId)) {
            throw new RuntimeException("File does not belong to the specified project.");
        }

        // Delete file from filesystem
        deleteFileFromSystem(file.getFilePath());

        // Delete file record from DB
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
}
