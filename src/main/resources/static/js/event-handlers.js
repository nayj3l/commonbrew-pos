// DOM ready event
document.addEventListener("DOMContentLoaded", function() {
    const firstCategoryButton = document.querySelector(".category-btn");
    if (firstCategoryButton) {
        const onclickAttr = firstCategoryButton.getAttribute("onclick");
        const categoryId = onclickAttr.match(/\d+/)[0];
        loadItems(categoryId);
    }

    document.querySelectorAll(".addon-checkbox").forEach((checkbox) => {
        checkbox.addEventListener("change", updateAddons);
    });
});