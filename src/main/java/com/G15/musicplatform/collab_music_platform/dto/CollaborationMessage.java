package com.G15.musicplatform.collab_music_platform.dto;

public class CollaborationMessage {
    private Long projectId;
    private String username;   // who made this change
    private String action;     // e.g., "CHANGE_TEMPO"
    private String payload;    // e.g., "120 BPM" or JSON with more details

    public CollaborationMessage() {
    }

    // Constructors, getters, setters, toString()

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "CollaborationMessage{" +
                "projectId=" + projectId +
                ", username='" + username + '\'' +
                ", action='" + action + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}