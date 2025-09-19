// Calculate order total
function calculateTotal() {
    let total = 0;
    currentOrder.items.forEach(item => {
        total += item.itemPrice * item.quantity;
        item.addons.forEach(addon => {
            total += addon.price * addon.quantity;
        });
    });
    currentOrder.total = total;
    document.getElementById("order-total").textContent =
        currentOrder.total.toFixed(2);
}

// Render order summary in modal
function renderModalOrder() {
    const panel = document.getElementById("modal-order-summary");

    if (currentOrder.items.length === 0) {
        panel.innerHTML = '<p class="text-center text-muted">No items yet</p>';
        document.getElementById("modal-order-total").textContent = "0.00";
        return;
    }

    let html = "";

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
                </div>`;

        // show addons under each item
        item.addons.forEach(addon => {
            html += `<div class="modal-order-item ms-4">
                        <div class="d-flex align-items-center">
                            <div class="quantity-controls">
                                <button class="btn btn-sm btn-danger" onclick="decrementAddon(${item.itemId}, ${addon.addonId})"><i class="bi bi-dash"></i></button>
                                <span class="mx-2">${addon.quantity}</span>
                                <button class="btn btn-sm btn-success" onclick="incrementAddon(${item.itemId}, ${addon.addonId})"><i class="bi bi-plus"></i></button>
                            </div>
                            <span class="ms-3">${addon.addonName}</span>
                            <div>₱${(addon.price * addon.quantity).toFixed(2)}</div>
                        </div>
                    </div>`;
        });

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
    // Items
    const items = currentOrder.items || [];

    document.getElementById("itemsVariantsIds").value = items
        .map(i => i.itemId)
        .join(",");

    document.getElementById("quantities").value = items
        .map(i => i.quantity)
        .join(",");

    const allAddons = items.flatMap(item => {
        if (!item.addons) return [];
        return item.addons.map(a => ({
            addonId: a.addonId,
            quantity: a.quantity,
            itemId: item.itemId,
            itemName: a.name,       // Use addon name here
            variantName: "-"         // Placeholder since no variant yet
        }));
    });

    document.getElementById("addonIds").value = allAddons
        .map(a => a.addonId)
        .join(",");

    document.getElementById("addonQuantities").value = allAddons
        .map(a => a.quantity)
        .join(",");

    // **Map each addon back to its parent item**
    document.getElementById("addonItemIds").value = allAddons
        .map(a => a.itemId)
        .join(",");

    // Submit form
    document.getElementById("order-form").submit();
}
