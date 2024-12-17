async function handleLogin() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const errorMessage = document.getElementById("errorMessage");

  // Clear previous error messages
  errorMessage.textContent = "";

  try {
    const response = await fetch("/api/users/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    if (response.ok) {
      // Store username in sessionStorage for later use
      sessionStorage.setItem("username", username);

      // Redirect to the dashboard
      window.location.href = "dashboard.html";
    } else {
      errorMessage.textContent =
        "Invalid username or password. Please try again.";
    }
  } catch (error) {
    console.error("Error:", error);
    errorMessage.textContent = "An error occurred. Please try again later.";
  }
}

function validateRegistrationFields(username, password, email) {
  // Add your validation logic, e.g.:
  if (!username || !password || !email) {
    alert("All fields are required.");
    return false;
  }
  return true;
}

async function handleRegister() {
  const username = document.getElementById("regUsername").value;
  const password = document.getElementById("regPassword").value;
  const email = document.getElementById("regEmail").value;

  if (!validateRegistrationFields(username, password, email)) {
    return; // Stop if validation fails
  }

  const errorMessage = document.getElementById("regErrorMessage");

  try {
    const response = await fetch("/api/users/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password, email }),
    });
    console.log(response);

    if (response.ok) {
      alert(`User '${username}' registered successfully! Please log in.`);
      // Redirect to the login page
      window.location.href = "index.html";
    } else {
      const errorText = await response.text();
      errorMessage.textContent = errorText;
    }
  } catch (error) {
    console.error("Error:", error);
    errorMessage.textContent = "An error occurred. Please try again later.";
  }
}
