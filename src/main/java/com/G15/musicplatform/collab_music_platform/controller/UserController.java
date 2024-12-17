package com.G15.musicplatform.collab_music_platform.controller;

import com.G15.musicplatform.collab_music_platform.model.FileMetadata;
import com.G15.musicplatform.collab_music_platform.model.User;
import com.G15.musicplatform.collab_music_platform.repository.FileMetadataRepository;
import com.G15.musicplatform.collab_music_platform.repository.UserRepository;
import com.G15.musicplatform.collab_music_platform.service.AudioService;

import jakarta.validation.Valid;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/users")
public class UserController {

    // Folder path to store uploaded files (set in application.properties)
    @Value("${upload.path}")
    private String uploadDir;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private AudioService audioService;


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        User existingUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify the password
        if (passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            return ResponseEntity.ok(existingUser.getUsername());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        // Basic validation
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is required");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be at least 6 characters");
        }
        if (user.getEmail() == null || !user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format");
        }

        // Check for duplicate username
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }

        // Save new user
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword())); // Plaintext for now; hash later
        newUser.setEmail(user.getEmail());

        userRepository.save(newUser);
        return ResponseEntity.ok("User registered successfully");
    }
    

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty. Please upload a valid audio file.");
        }

        try {
            // Generate file name and replace invalid characters
            String fileName = LocalDateTime.now()
                    .toString()
                    .replace(":", "-")
                    .replace(".", "-")
                    + "_" + file.getOriginalFilename();

            // Create directory if it does not exist
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();  // This ensures the uploads folder is created
            }

            File destination = new File(uploadDirectory, fileName);

            // Save file
            file.transferTo(destination);

            // Save metadata to database
            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(fileName);
            metadata.setFilePath(destination.getAbsolutePath());
            metadata.setFileSize(file.getSize());
            metadata.setUploadTime(LocalDateTime.now());

            fileMetadataRepository.save(metadata);

            return ResponseEntity.ok("File uploaded successfully: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        }
    }
        
    @PostMapping("/process")
    public ResponseEntity<String> processAudio(@RequestParam("fileName") String fileName) {
        String filePath = uploadDir + "/" + fileName;

        // Call audio processing logic
        String result = audioService.adjustVolume(filePath, 1.5f); // Example: Increase volume by 50%

        return ResponseEntity.ok(result);
    }

    @GetMapping("/files")
    public ResponseEntity<?> listUploadedFiles() {
        try {
            // Fetch all metadata from the database
            List<FileMetadata> files = fileMetadataRepository.findAll();

            if (files.isEmpty()) {
                return ResponseEntity.ok("No files uploaded yet.");
            }

            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to fetch files: " + e.getMessage());
        }
    }

}