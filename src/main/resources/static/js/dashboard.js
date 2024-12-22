document.addEventListener("DOMContentLoaded", () => {
  const projectList = document.getElementById("projectList");
  const createProjectBtn = document.getElementById("createProjectBtn");
  const uploadSection = document.getElementById("uploadSection");
  const uploadBtn = document.getElementById("uploadBtn");
  const fileInput = document.getElementById("fileInput");
  const fileList = document.getElementById("fileList");
  const showFilesBtn = document.getElementById("showFilesBtn");
  const userList = document.getElementById("userList");
  // Buttons for adding/removing users:
  const addUserBtn = document.getElementById("addUserBtn");
  const removeUserBtn = document.getElementById("removeUserBtn");

  let currentProjectId = null;

  // Display welcome message
  const username = sessionStorage.getItem("username");
  if (!username) {
    alert("Please log in first!");
    window.location.href = "index.html";
  }
  document.getElementById(
    "welcomeMessage"
  ).textContent = `Welcome, ${username}!!!`;

  /**
   * Check if the logged-in user is the PROJECT_OWNER for the current project.
   * If yes, show the Add/Remove user buttons; if no, hide them.
   */
  const checkIfUserIsOwner = async (projectId, username) => {
    try {
      // e.g. GET /api/projects/:projectId/role?username=rahul
      const response = await fetch(
        `/api/projects/${projectId}/role?username=${username}`
      );
      const role = await response.text(); // e.g., "PROJECT_OWNER", "CONTRIBUTOR", etc.

      if (role === "PROJECT_OWNER") {
        addUserBtn.style.display = "inline-block";
        removeUserBtn.style.display = "inline-block";
      } else {
        addUserBtn.style.display = "none";
        removeUserBtn.style.display = "none";
      }
    } catch (error) {
      console.error("Error checking user role:", error);
      addUserBtn.style.display = "none";
      removeUserBtn.style.display = "none";
    }
  };

  /**
   * Fetch and display projects for the logged-in user.
   */
  const fetchProjects = async () => {
    try {
      const response = await fetch(`/api/projects?username=${username}`);
      const projects = await response.json();
      projectList.innerHTML = "";

      if (projects.length > 0) {
        projects.forEach((project) => {
          const listItem = document.createElement("li");

          // Project name
          const projectNameSpan = document.createElement("span");
          projectNameSpan.textContent = project.name;
          projectNameSpan.style.marginRight = "10px";
          listItem.appendChild(projectNameSpan);

          // Delete project button
          const deleteProjectBtn = document.createElement("button");
          deleteProjectBtn.textContent = "Delete";
          Object.assign(deleteProjectBtn.style, {
            background: "#dc3545",
            color: "#fff",
            marginLeft: "10px",
            border: "none",
            padding: "5px 10px",
            borderRadius: "4px",
            cursor: "pointer",
          });

          // Delete project event
          deleteProjectBtn.addEventListener("click", async (e) => {
            e.stopPropagation();
            const confirmDelete = confirm(
              `Are you sure you want to delete project "${project.name}"?`
            );
            if (!confirmDelete) return;

            try {
              // Pass username => role check in backend
              const delResponse = await fetch(
                `/api/projects/${project.id}?username=${username}`,
                { method: "DELETE" }
              );
              if (delResponse.ok) {
                alert("Project deleted successfully.");
                fetchProjects(); // Refresh project list
                if (project.id === currentProjectId) {
                  currentProjectId = null;
                  fileList.innerHTML = "";
                  uploadSection.style.display = "none";
                  userList.innerHTML = "";
                }
              } else {
                const errorMsg = await delResponse.text();
                alert("Failed to delete project: " + errorMsg);
              }
            } catch (err) {
              console.error("Error deleting project:", err);
            }
          });

          // Selecting the project
          listItem.addEventListener("click", async () => {
            currentProjectId = project.id;
            uploadSection.style.display = "block";

            // 1) Fetch the user list
            await fetchProjectUsers(currentProjectId);
            // 2) Check if this logged-in user is the PROJECT_OWNER
            await checkIfUserIsOwner(currentProjectId, username);
          });

          listItem.appendChild(deleteProjectBtn);
          projectList.appendChild(listItem);
        });
      } else {
        projectList.innerHTML =
          "<li>No projects available. Create a new one!</li>";
      }
    } catch (error) {
      console.error("Failed to fetch projects:", error);
    }
  };

  /**
   * Fetch and display files for a specific project.
   */
  const fetchProjectFiles = async (projectId) => {
    try {
      const response = await fetch(`/api/projects/${projectId}/files`);
      const files = await response.json();

      fileList.innerHTML = ""; // Clear previous files

      if (files.length > 0) {
        files.forEach((file) => {
          const listItem = document.createElement("li");

          // File info
          const fileInfoSpan = document.createElement("span");
          fileInfoSpan.textContent = `${file.id} - ${file.fileName} - ${file.filePath}`;
          fileInfoSpan.style.marginRight = "10px";
          listItem.appendChild(fileInfoSpan);

          // Delete file button
          const deleteFileBtn = document.createElement("button");
          deleteFileBtn.textContent = "Delete";
          Object.assign(deleteFileBtn.style, {
            background: "#dc3545",
            color: "#fff",
            border: "none",
            padding: "5px 10px",
            borderRadius: "4px",
            cursor: "pointer",
          });

          // Delete file event
          deleteFileBtn.addEventListener("click", async (e) => {
            e.stopPropagation();
            const confirmDelete = confirm(
              `Are you sure you want to delete the file "${file.fileName}"?`
            );
            if (!confirmDelete) return;

            try {
              // Must pass ?username= for role check
              const delFileResponse = await fetch(
                `/api/projects/${projectId}/files/${file.id}?username=${username}`,
                { method: "DELETE" }
              );
              if (delFileResponse.ok) {
                alert("File deleted successfully.");
                fetchProjectFiles(projectId); // Refresh file list
              } else {
                const errorMsg = await delFileResponse.text();
                alert("Failed to delete file: " + errorMsg);
              }
            } catch (err) {
              console.error("Error deleting file:", err);
            }
          });

          listItem.appendChild(deleteFileBtn);
          fileList.appendChild(listItem);
        });
      } else {
        fileList.innerHTML = "<li>No files uploaded for this project.</li>";
      }
    } catch (error) {
      console.error("Failed to fetch project files:", error);
    }
  };

  /**
   * Fetch and display all users assigned to a project (with roles).
   * This requires an endpoint like /api/projects/{projectId}/users
   */
  const fetchProjectUsers = async (projectId) => {
    try {
      const response = await fetch(`/api/projects/${projectId}/users`);
      const users = await response.json();

      userList.innerHTML = ""; // Clear previous list

      if (Array.isArray(users) && users.length > 0) {
        users.forEach((user) => {
          const li = document.createElement("li");
          // e.g. "rahul - PROJECT_OWNER"
          li.textContent = `${user.username} - ${user.role}`;
          userList.appendChild(li);
        });
      } else {
        userList.innerHTML = "<li>No users in this project.</li>";
      }
    } catch (error) {
      console.error("Failed to fetch project users:", error);
      userList.innerHTML = "<li>Failed to load project users.</li>";
    }
  };

  /**
   * Create a new project. The backend sets the creator as PROJECT_OWNER.
   */
  createProjectBtn.addEventListener("click", async () => {
    const projectName = prompt("Enter the project name:");
    if (!projectName) return;

    try {
      const response = await fetch(
        `/api/projects?name=${encodeURIComponent(
          projectName
        )}&username=${username}`,
        { method: "POST" }
      );
      const result = await response.text();
      alert(result);
      fetchProjects(); // Refresh projects list
    } catch (error) {
      console.error("Failed to create project:", error);
    }
  });

  /**
   * Upload file (OWNER/CONTRIBUTOR).
   */
  uploadBtn.addEventListener("click", async () => {
    if (!currentProjectId) {
      alert("Please select a project first.");
      return;
    }

    const files = fileInput.files;
    if (files.length === 0) {
      alert("Please select a file to upload.");
      return;
    }

    const formData = new FormData();
    formData.append("file", files[0]); // For MVP, one file at a time

    try {
      const response = await fetch(
        `/api/projects/${currentProjectId}/upload?username=${username}`,
        {
          method: "POST",
          body: formData,
        }
      );
      const result = await response.text();
      alert(result);
      // Refresh the file list
      fetchProjectFiles(currentProjectId);
    } catch (error) {
      console.error("Failed to upload file:", error);
    }
  });

  /**
   * "Show Files" button
   */
  showFilesBtn.addEventListener("click", () => {
    if (!currentProjectId) {
      alert("Please select a project first.");
      return;
    }
    fetchProjectFiles(currentProjectId);
  });

  /**
   * Add user to the project by searching their username first.
   */
  addUserBtn.addEventListener("click", async () => {
    if (!currentProjectId) {
      alert("Please select a project first.");
      return;
    }

    // Prompt for the username
    const targetUsername = prompt("Enter the username you want to add:");
    if (!targetUsername) return;

    // 1) Search if user exists in the backend
    let userFound = null;
    try {
      // This endpoint must exist in your backend:
      // e.g. GET /api/users/search?username=someString
      const searchResponse = await fetch(
        `/api/users/search?username=${targetUsername}`
      );
      if (searchResponse.ok) {
        // Should contain {id: 123, username: "bob"} if found
        userFound = await searchResponse.json();
      } else {
        // 404 or similar
        const errorMsg = await searchResponse.text();
        alert(`User not found: ${errorMsg}`);
        return;
      }
    } catch (error) {
      console.error("Error searching for user:", error);
      alert("Failed to search for user. Please try again.");
      return;
    }

    if (!userFound || !userFound.id) {
      alert("No such user present.");
      return;
    }

    // 2) Ask for role
    const role = prompt(
      "Enter the role (PROJECT_OWNER, CONTRIBUTOR, REVIEWER)?",
      "CONTRIBUTOR"
    );
    if (!role) return;

    // 3) Assign the role
    try {
      const response = await fetch(
        `/api/projects/${currentProjectId}/assign-role?userId=${userFound.id}&role=${role}&username=${username}`,
        { method: "POST" }
      );
      if (response.ok) {
        alert(`User '${targetUsername}' assigned role: ${role}`);
        // Refresh user list
        fetchProjectUsers(currentProjectId);
      } else {
        const errorMsg = await response.text();
        alert("Failed to assign role: " + errorMsg);
      }
    } catch (error) {
      console.error("Error assigning role:", error);
    }
  });

  /**
   * Remove user from the project, by searching the username first.
   */
  removeUserBtn.addEventListener("click", async () => {
    if (!currentProjectId) {
      alert("Please select a project first.");
      return;
    }

    const targetUsername = prompt(
      "Enter the username you want to remove from the project:"
    );
    if (!targetUsername) return;

    // 1) Search if user exists
    let userFound = null;
    try {
      const searchResponse = await fetch(
        `/api/users/search?username=${targetUsername}`
      );
      if (searchResponse.ok) {
        userFound = await searchResponse.json();
      } else {
        const errorMsg = await searchResponse.text();
        alert(`User not found: ${errorMsg}`);
        return;
      }
    } catch (error) {
      console.error("Error searching for user:", error);
      alert("Failed to search for user. Please try again.");
      return;
    }

    if (!userFound || !userFound.id) {
      alert("No such user present.");
      return;
    }

    // 2) Revoke the role
    try {
      const response = await fetch(
        `/api/projects/${currentProjectId}/revoke-role?userId=${userFound.id}&username=${username}`,
        { method: "POST" }
      );
      if (response.ok) {
        alert(`User '${targetUsername}' removed from project.`);
        fetchProjectUsers(currentProjectId);
      } else {
        const errorMsg = await response.text();
        alert("Failed to remove user: " + errorMsg);
      }
    } catch (error) {
      console.error("Error revoking role:", error);
    }
  });

  // Initialize
  fetchProjects();
});
