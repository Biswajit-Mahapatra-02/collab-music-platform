document.addEventListener("DOMContentLoaded", () => {
  const projectList = document.getElementById("projectList");
  const createProjectBtn = document.getElementById("createProjectBtn");
  const uploadSection = document.getElementById("uploadSection");
  const uploadBtn = document.getElementById("uploadBtn");
  const fileInput = document.getElementById("fileInput");
  const fileList = document.getElementById("fileList");
  const showFilesBtn = document.getElementById("showFilesBtn");

  let currentProjectId = null;

  // Display welcome message
  const username = sessionStorage.getItem("username");
  if (!username) {
    alert("Please log in first!");
    window.location.href = "index.html";
  }

  document.getElementById(
    "welcomeMessage"
  ).textContent = `Welcome, ${username}!`;

  // Fetch and display projects
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
          deleteProjectBtn.style.background = "#dc3545";
          deleteProjectBtn.style.color = "#fff";
          deleteProjectBtn.style.marginLeft = "10px";
          deleteProjectBtn.style.border = "none";
          deleteProjectBtn.style.padding = "5px 10px";
          deleteProjectBtn.style.borderRadius = "4px";
          deleteProjectBtn.style.cursor = "pointer";

          deleteProjectBtn.addEventListener("click", async (e) => {
            e.stopPropagation(); // Prevent triggering project selection
            const confirmDelete = confirm(
              `Are you sure you want to delete project "${project.name}"?`
            );
            if (!confirmDelete) return;

            try {
              const delResponse = await fetch(`/api/projects/${project.id}`, {
                method: "DELETE",
              });
              if (delResponse.ok) {
                alert("Project deleted successfully.");
                fetchProjects(); // Refresh project list
                if (project.id === currentProjectId) {
                  currentProjectId = null;
                  fileList.innerHTML = "";
                  uploadSection.style.display = "none";
                }
              } else {
                alert("Failed to delete project.");
              }
            } catch (err) {
              console.error("Error deleting project:", err);
            }
          });

          listItem.addEventListener("click", () => {
            currentProjectId = project.id;
            uploadSection.style.display = "block";
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

  // Fetch and display files for a specific project
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
          deleteFileBtn.style.background = "#dc3545";
          deleteFileBtn.style.color = "#fff";
          deleteFileBtn.style.border = "none";
          deleteFileBtn.style.padding = "5px 10px";
          deleteFileBtn.style.borderRadius = "4px";
          deleteFileBtn.style.cursor = "pointer";

          deleteFileBtn.addEventListener("click", async (e) => {
            e.stopPropagation();
            const confirmDelete = confirm(
              `Are you sure you want to delete the file "${file.fileName}"?`
            );
            if (!confirmDelete) return;

            try {
              const delFileResponse = await fetch(
                `/api/projects/${projectId}/files/${file.id}`,
                {
                  method: "DELETE",
                }
              );
              if (delFileResponse.ok) {
                alert("File deleted successfully.");
                fetchProjectFiles(projectId); // Refresh file list
              } else {
                alert("Failed to delete file.");
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

  // Create a new project
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

  // Upload files to the selected project
  uploadBtn.addEventListener("click", async () => {
    if (!currentProjectId) {
      alert("Please select a project first.");
      return;
    }

    const files = fileInput.files;
    if (files.length === 0) {
      alert("Please select files to upload.");
      return;
    }

    const formData = new FormData();
    formData.append("file", files[0]); // For MVP, upload one file at a time

    try {
      const response = await fetch(`/api/projects/${currentProjectId}/upload`, {
        method: "POST",
        body: formData,
      });
      const result = await response.text();
      alert(result);
      // After uploading, you can auto-fetch or rely on the Show Files button:
      fetchProjectFiles(currentProjectId);
    } catch (error) {
      console.error("Failed to upload file:", error);
    }
  });

  // Add event listener to the "Show Files" button
  showFilesBtn.addEventListener("click", () => {
    if (!currentProjectId) {
      alert("Please select a project first.");
      return;
    }
    fetchProjectFiles(currentProjectId);
  });

  // Initialize
  fetchProjects();
});
