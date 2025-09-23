// Load menu items for a category
function loadItems(menuId) {
    fetch("/order/items/" + menuId)
        .then((response) => {
            if (!response.ok) {
                throw new Error("Network response was not ok");
            }
            return response.json();
        })
        .then((items) => {
            const selectFlavor = document.getElementById("itemsModalBody");
            selectFlavor.innerHTML = "";

            if (!items || items.length === 0) {
                selectFlavor.innerHTML = '<p class="text-center">No items available in this category</p>';
                return;
            }

            items.forEach((item) => {
                const itemDiv = document.createElement("div");
                itemDiv.className = "mb-2";

                const flavorBtn = document.createElement("button");
                flavorBtn.className = "btn btn-outline-success item-btn";
                flavorBtn.textContent = item.name;
                flavorBtn.dataset.itemId = item.id;
                flavorBtn.dataset.itemPrice = item.basePrice;
                flavorBtn.onclick = function() {
                    addToOrder(this);
                    bootstrap.Modal.getInstance(document.getElementById("itemsModal")).hide();
                };

                itemDiv.appendChild(flavorBtn);
                selectFlavor.appendChild(itemDiv);
            });

            // show modal
            const modal = new bootstrap.Modal(document.getElementById("itemsModal"));
            modal.show();
        })
        .catch((err) => {
            console.error("Error loading items:", err);
            document.getElementById("itemsModalBody").innerHTML =
                '<p class="text-center text-danger">Error loading items. Please try again.</p>';
        });
}
