// Toggle categories section
function toggleCategories() {
    const categoriesContainer = document.getElementById("categories-container");
    const toggleIcon = document.getElementById("categories-toggle");

    categoriesContainer.classList.toggle("collapsed");
    toggleIcon.classList.toggle("collapsed");
}

// Category button selection handling
(function() {
    const container = document.querySelector('.category-container');
    if (!container) return;

    const buttons = container.querySelectorAll('.category-btn');

    // initialize aria-pressed for accessibility
    buttons.forEach(b => b.setAttribute('aria-pressed', 'false'));

    buttons.forEach(btn => {
        btn.addEventListener('click', () => {
            const already = btn.classList.contains('selected');

            // deselect if clicked again
            if (already) {
                btn.classList.remove('selected');
                btn.setAttribute('aria-pressed', 'false');
                container.classList.remove('has-selection');
                return;
            }

            // select clicked, deselect others
            buttons.forEach(b => {
                b.classList.toggle('selected', b === btn);
                b.setAttribute('aria-pressed', b === btn ? 'true' : 'false');
            });

            // add container class so CSS dims other buttons
            container.classList.add('has-selection');
        });

        // keyboard: support Enter and Space
        btn.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                btn.click();
            }
        });
    });

    // Optional: when user moves mouse out of entire container, remove hover-dim
    container.addEventListener('mouseleave', () => {
        // no-op; CSS handles container:hover dimming.
    });
})();