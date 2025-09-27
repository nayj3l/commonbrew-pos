// Calculate order total
function calculateTotal() {
    let total = 0;

    // Items total
    currentOrder.items.forEach(item => {
        total += item.itemPrice * item.quantity;
    });

    // Addons total (global case)
    if (currentOrder.addons && Array.isArray(currentOrder.addons)) {
        currentOrder.addons.forEach(addon => {
            total += addon.price * addon.quantity;
        });
    }

    currentOrder.total = total;
    
    document.getElementById("order-total").textContent =
        `₱${currentOrder.total.toFixed(2)}`;
    return total;
}


// Render order summary in modal
function renderModalOrder() {
    const panel = document.getElementById("modal-order-summary");

    if (currentOrder.items.length === 0) {
        panel.innerHTML = '<p class="text-center text-muted">No items yet</p>';
        document.getElementById("modal-order-total").textContent = "0.00";
        return;
    }

    let html = "";// Show items
    currentOrder.items.forEach(item => {
        html += `<div class="modal-order-item">
                    <div class="d-flex align-items-center">
                        <div class="quantity-controls">
                            <button class="btn btn-sm btn-danger" onclick="decrementItem(${item.itemId})"><i class="bi bi-dash"></i></button>
                            <span class="mx-2">${item.quantity}</span>
                            <button class="btn btn-sm btn-success" onclick="incrementItem(${item.itemId})"><i class="bi bi-plus"></i></button>
                        </div>
                        <span class="ms-3">${item.itemName}</span>
                        <div>₱${(item.itemPrice * item.quantity).toFixed(2)}</div>
                    </div>
                </div>`;
    });

    // Show global addons
    currentOrder.addons.forEach(addon => {
        html += `<div class="modal-order-item ms-4">
                    <div class="d-flex align-items-center">
                        <div class="quantity-controls">
                            <button class="btn btn-sm btn-danger" onclick="decrementAddon(${addon.addonId})"><i class="bi bi-dash"></i></button>
                            <span class="mx-2">${addon.quantity}</span>
                            <button class="btn btn-sm btn-success" onclick="incrementAddon(${addon.addonId})"><i class="bi bi-plus"></i></button>
                        </div>
                        <span class="ms-3">${addon.addonName}</span>
                        <div>₱${(addon.price * addon.quantity).toFixed(2)}</div>
                    </div>
                </div>`;
    });

    panel.innerHTML = html;
    document.getElementById("modal-order-total").textContent = currentOrder.total.toFixed(2);
}

// Show confirmation modal
function confirmOrder() {
    if (currentOrder.items.length === 0 && currentOrder.addons.length === 0) {
        alert("No items in the order!");
        return;
    }

    renderModalOrder();
    
    let modal = new bootstrap.Modal(document.getElementById("orderConfirmModal"));
    modal.show();
}

// Submit the confirmed order
function submitConfirmedOrder() {
    const items = currentOrder.items || [];
    const allAddons  = currentOrder.addons || [];

    document.getElementById("itemIds").value = items
        .map(i => i.itemId)
        .join(",");

    document.getElementById("variantsIds").value = items
        .map(i => i.itemId)
        .join(",");

    document.getElementById("quantities").value = items
        .map(i => i.quantity)
        .join(",");

    document.getElementById("addonIds").value = allAddons
        .map(a => a.addonId)
        .join(",");

    document.getElementById("addonQuantities").value = allAddons
        .map(a => a.quantity)
        .join(",");

    console.log("=== FORM VALUES ===");
    console.log("itemIds:", document.getElementById("itemIds").value);
    console.log("variantsIds input value:", document.getElementById("variantsIds").value);
    console.log("quantities input value:", document.getElementById("quantities").value);
    console.log("addonIds input value:", document.getElementById("addonIds").value);
    console.log("addonQuantities input value:", document.getElementById("addonQuantities").value);

    document.getElementById("order-form").submit();
}

