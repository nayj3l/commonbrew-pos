// Load drinks for a category
function loadDrinks(categoryId) {
    fetch("/order/drinks/" + categoryId)
        .then((response) => {
            if (!response.ok) {
                throw new Error("Network response was not ok");
            }
            return response.json();
        })
        .then((drinks) => {
            const container = document.getElementById("drinks-container");
            container.innerHTML = "";

            if (drinks.length === 0) {
                container.innerHTML =
                    '<p class="text-center">No drinks available in this category</p>';
                return;
            }

            drinks.forEach((drink) => {
                const drinkItem = document.createElement("div");
                drinkItem.className = "drink-item";

                const btn = document.createElement("button");
                btn.className = "btn btn-outline-success drink-btn";
                btn.textContent = drink.drinkName;
                btn.dataset.drinkId = drink.drinkId;
                btn.dataset.drinkPrice = drink.basePrice;
                btn.onclick = function() {
                    addToOrder(this);
                };

                const priceDiv = document.createElement("div");
                priceDiv.className = "text-center mt-1";

                drinkItem.appendChild(btn);
                drinkItem.appendChild(priceDiv);
                container.appendChild(drinkItem);
            });
        })
        .catch((err) => {
            console.error("Error loading drinks:", err);
            const container = document.getElementById("drinks-container");
            container.innerHTML =
                '<p class="text-center">Error loading drinks. Please try again.</p>';
        });
}