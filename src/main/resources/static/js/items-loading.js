// Load menu items for a category
function loadItems(categoryId) {
    fetch("/order/items/" + categoryId)
        .then((response) => {
            if (!response.ok) {
                throw new Error("Network response was not ok");
            }
            return response.json();
        })
        .then((items) => {
            const container = document.getElementById("items-container");
            container.innerHTML = "";

            if (items.length === 0) {
                container.innerHTML =
                    '<p class="text-center">No items available in this category</p>';
                return;
            }

            items.forEach((item) => {
                const itemItem = document.createElement("div");
                itemItem.className = "item-item";

                const btn = document.createElement("button");
                btn.className = "btn btn-outline-success item-btn";
                btn.textContent = item.itemName;
                btn.dataset.itemId = item.itemId;
                btn.dataset.itemPrice = item.basePrice;
                btn.onclick = function() {
                    addToOrder(this);
                };

                const priceDiv = document.createElement("div");
                priceDiv.className = "text-center mt-1";

                itemItem.appendChild(btn);
                itemItem.appendChild(priceDiv);
                container.appendChild(itemItem);
            });
        })
        .catch((err) => {
            console.error("Error loading items:", err);
            const container = document.getElementById("items-container");
            container.innerHTML =
                '<p class="text-center">Error loading items. Please try again.</p>';
        });
}