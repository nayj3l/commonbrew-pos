// DOM ready event
document.addEventListener("DOMContentLoaded", function() {
    document.querySelectorAll(".addon-checkbox").forEach((checkbox) => {
        checkbox.addEventListener("change", updateAddons);
    });
});