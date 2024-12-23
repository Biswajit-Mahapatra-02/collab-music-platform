package com.G15.musicplatform.collab_music_platform.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.G15.musicplatform.collab_music_platform.dto.CollaborationMessage;

@Controller
public class CollaborationController {

    /**
     * Example endpoint: clients send to /app/project/{projectId}/update
     * We broadcast to /topic/project/{projectId}
     */
    @MessageMapping("/project/{projectId}/update")
    @SendTo("/topic/project/{projectId}")
    public CollaborationMessage handleProjectUpdate(CollaborationMessage message) {
        // If needed, you can do role checks or persist the changes in a DB.
        System.out.println("Received real-time update: " + message);

        // Return the same (or modified) message => broadcast to all subscribers
        return message;
    }
}
