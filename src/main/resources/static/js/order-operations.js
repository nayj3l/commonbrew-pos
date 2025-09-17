// Add drink to order
function addToOrder(button) {
    const drinkId = button.getAttribute("data-drink-id");
    const drinkName = button.textContent;
    const drinkPrice = parseFloat(button.getAttribute("data-drink-price"));
    const quantity = 1;

    const existingItemIndex = currentOrder.items.findIndex(
        (item) => item.drinkId === drinkId
    );

    if (existingItemIndex >= 0) {
        currentOrder.items[existingItemIndex].quantity += 1;
    } else {
        currentOrder.items.push({
            drinkId,
            drinkName,
            drinkPrice,
            quantity,
        });
    }

    calculateTotal();
    renderOrder();
}

// Update addons in order
function updateAddons() {
    currentOrder.addons = [];

    document
        .querySelectorAll(".addon-checkbox:checked")
        .forEach((checkbox) => {
            const addonId = checkbox.value;
            const addonName = checkbox.getAttribute("data-addon-name");
            const addonPrice = parseFloat(
                checkbox.getAttribute("data-addon-price")
            );

            const existing = currentOrder.addons.find(
                (a) => a.addonId == addonId
            );
            if (existing) {
                existing.quantity += 1;
            } else {
                currentOrder.addons.push({
                    addonId,
                    addonName,
                    price: addonPrice,
                    quantity: 1,
                });
            }
        });

    calculateTotal();
    renderOrder();
}

// Increment item quantity
function incrementItem(drinkId) {
    const item = currentOrder.items.find((item) => item.drinkId == drinkId);
    if (item) {
        item.quantity += 1;
        calculateTotal();
        renderOrder();
    }
}

// Decrement item quantity
function decrementItem(drinkId) {
    const itemIndex = currentOrder.items.findIndex(
        (item) => item.drinkId == drinkId
    );
    if (itemIndex >= 0) {
        const item = currentOrder.items[itemIndex];
        if (item.quantity > 1) {
            item.quantity -= 1;
        } else {
            currentOrder.items.splice(itemIndex, 1);
        }
        calculateTotal();
        renderOrder();
    }
}

// Increment addon quantity
function incrementAddon(addonId) {
    const addon = currentOrder.addons.find((a) => a.addonId == addonId);
    if (addon) {
        addon.quantity += 1;
        calculateTotal();
        renderOrder();
    }
}

// Decrement addon quantity
function decrementAddon(addonId) {
    const index = currentOrder.addons.findIndex(
        (a) => a.addonId == addonId
    );
    if (index >= 0) {
        const addon = currentOrder.addons[index];
        if (addon.quantity > 1) {
            addon.quantity -= 1;
        } else {
            currentOrder.addons.splice(index, 1);
            const checkbox = document.getElementById("addon-" + addonId);
            if (checkbox) checkbox.checked = false;
        }
        calculateTotal();
        renderOrder();
    }
}