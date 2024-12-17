package com.G15.musicplatform.collab_music_platform.repository;

import com.G15.musicplatform.collab_music_platform.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
}