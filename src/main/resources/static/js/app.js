document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".needs-validation").forEach((form) => {
    form.addEventListener("submit", (event) => {
      if (!form.checkValidity()) {
        event.preventDefault();
        event.stopPropagation();
      }
      form.classList.add("was-validated");
    });
  });

  document.querySelectorAll("[data-password-toggle]").forEach((button) => {
    button.addEventListener("click", () => {
      const input = document.getElementById(button.getAttribute("aria-controls"));
      const showPassword = input.type === "password";
      input.type = showPassword ? "text" : "password";
      button.setAttribute("aria-label", showPassword ? "Hide password" : "Show password");
      const icon = button.querySelector("[data-password-icon]");
      icon.classList.toggle("bi-eye", !showPassword);
      icon.classList.toggle("bi-eye-slash", showPassword);
    });
  });
});
