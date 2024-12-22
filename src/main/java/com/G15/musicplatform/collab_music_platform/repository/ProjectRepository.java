package com.G15.musicplatform.collab_music_platform.repository;

import com.G15.musicplatform.collab_music_platform.model.Project;
import com.G15.musicplatform.collab_music_platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUser(User user);

    // NEW: find all projects where the given user is either the .user (creator)
    // or appears in the userRoles map for that project.
    @Query("SELECT p FROM Project p LEFT JOIN p.userRoles roles " +
           "WHERE key(roles) = :user OR p.user = :user")
    List<Project> findProjectsWhereUserHasRoleOrIsOwner(@Param("user") User user);
}
