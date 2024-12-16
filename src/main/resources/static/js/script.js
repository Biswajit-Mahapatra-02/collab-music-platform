async function handleLogin() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const errorMessage = document.getElementById("errorMessage");
  const loginSection = document.getElementById("loginSection");
  const welcomeSection = document.getElementById("welcomeSection");

  // Clear previous error messages
  errorMessage.textContent = "";

  try {
    const response = await fetch("/api/users/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    if (response.ok) {
      const fetchedUsername = await response.text();

      // Hide login form and display welcome message
      loginSection.style.display = "none";
      welcomeSection.style.display = "block";
      document.getElementById(
        "welcomeMessage"
      ).textContent = `Welcome to the Music Production Platform, ${fetchedUsername}!`;
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
  const errorMessage = document.getElementById("regErrorMessage");

  // Clear any previous errors
  errorMessage.textContent = "";

  // Validation logic
  if (!username || username.trim() === "") {
    errorMessage.textContent = "Username is required!";
    return false;
  }
  if (!password || password.length < 6) {
    errorMessage.textContent = "Password must be at least 6 characters!";
    return false;
  }
  if (!email || !/^\S+@\S+\.\S+$/.test(email)) {
    errorMessage.textContent = "Please enter a valid email!";
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
  const registrationSection = document.getElementById("registrationSection");
  const successSection = document.getElementById("successSection");

  try {
    const response = await fetch("/api/users/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password, email }),
    });

    if (response.ok) {
      const successMessage = document.getElementById("successMessage");
      successMessage.textContent = `User '${username}' registered successfully!`;
      registrationSection.style.display = "none";
      successSection.style.display = "block";
    } else {
      const errorText = await response.text();
      errorMessage.textContent = errorText;
    }
  } catch (error) {
    console.error("Error:", error);
    errorMessage.textContent = "An error occurred. Please try again later.";
  }
}
