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
            const container = document.getElementById("itemsModalBody");
            container.innerHTML = "";

            if (!items || items.length === 0) {
                container.innerHTML = '<p class="text-center">No items available in this category</p>';
                return;
            }

            items.forEach((item) => {
                const itemDiv = document.createElement("div");
                itemDiv.className = "mb-2";

                const btn = document.createElement("button");
                btn.className = "btn btn-outline-success item-btn";
                btn.textContent = item.name;
                btn.dataset.itemId = item.id;
                btn.dataset.itemPrice = item.basePrice;
                btn.onclick = function() {
                    addToOrder(this);
                    bootstrap.Modal.getInstance(document.getElementById("itemsModal")).hide();
                };

                itemDiv.appendChild(btn);
                container.appendChild(itemDiv);
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
