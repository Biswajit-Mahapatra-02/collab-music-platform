/**
 * Handle user login and store the username in sessionStorage.
 */
async function handleLogin() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const errorMessage = document.getElementById("errorMessage");

  // Clear any previous error messages
  errorMessage.textContent = "";

  try {
    const response = await fetch("/api/users/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    if (response.ok) {
      // Store username in sessionStorage for later use (role checks, etc.)
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

/**
 * Simple client-side validation for registration fields.
 */
function validateRegistrationFields(username, password, email) {
  if (!username || !password || !email) {
    alert("All fields are required.");
    return false;
  }
  // You could add stronger validation here...
  return true;
}

/**
 * Handle user registration and then redirect to login if successful.
 */
async function handleRegister() {
  const username = document.getElementById("regUsername").value;
  const password = document.getElementById("regPassword").value;
  const email = document.getElementById("regEmail").value;
  const errorMessage = document.getElementById("regErrorMessage");

  // Clear any previous error messages
  errorMessage.textContent = "";

  // Validate fields
  if (!validateRegistrationFields(username, password, email)) {
    return;
  }

  try {
    const response = await fetch("/api/users/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password, email }),
    });

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
