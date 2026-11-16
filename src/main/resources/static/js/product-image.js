document.querySelector("[data-image-input]")?.addEventListener("change", event => {
  const file = event.target.files[0];
  const preview = document.querySelector("[data-image-preview]");
  if (!file) return;
  if (!["image/jpeg", "image/png"].includes(file.type) || file.size > 5 * 1024 * 1024) {
    event.target.setCustomValidity("Choose a JPG or PNG image up to 5 MB.");
    event.target.reportValidity();
    event.target.value = "";
    return;
  }
  event.target.setCustomValidity("");
  preview.src = URL.createObjectURL(file);
  preview.style.display = "block";
  preview.onload = () => URL.revokeObjectURL(preview.src);
});
