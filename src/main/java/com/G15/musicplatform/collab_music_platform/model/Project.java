package com.G15.musicplatform.collab_music_platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDateTime createdAt = LocalDateTime.now();

    // "Owner" or "Creator"
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectFile> projectFiles;

    @ElementCollection
    @CollectionTable(name = "project_roles", joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "role")
    private Map<User, String> userRoles = new HashMap<>();

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setProjectFiles(List<ProjectFile> projectFiles) {
        this.projectFiles = projectFiles;
    }

    public List<ProjectFile> getProjectFiles() {
        return projectFiles;
    }

    public void setUserRoles(Map<User, String> userRoles) {
        this.userRoles = userRoles;
    }

    public Map<User, String> getUserRoles() {
        return userRoles;
    }
}
