// Calculate order total
function calculateTotal() {
    let itemsTotal = currentOrder.items.reduce((total, item) => {
        return total + item.itemPrice * item.quantity;
    }, 0);

    let addonsTotal = currentOrder.addons.reduce((total, addon) => {
        return total + addon.price * addon.quantity;
    }, 0);

    currentOrder.total = itemsTotal + addonsTotal;
    document.getElementById("order-total").textContent =
        currentOrder.total.toFixed(2);
}

// Render order summary in modal
function renderModalOrder() {
    const panel = document.getElementById("modal-order-summary");

    if (currentOrder.items.length === 0 && currentOrder.addons.length === 0) {
        panel.innerHTML = '<p class="text-center text-muted">No items yet</p>';
        document.getElementById("modal-order-total").textContent = "0.00";
        return;
    }

    let html = "";

    currentOrder.items.forEach((item) => {
        html += `
        <div class="modal-order-item">
            <div class="d-flex align-items-center">
                <div class="quantity-controls">
                    <button class="btn btn-sm btn-danger" onclick="decrementItem(${item.itemId})"><i class="bi bi-dash"></i></button>
                    <span class="mx-2">${item.quantity}</span>
                    <button class="btn btn-sm btn-success" onclick="incrementItem(${item.itemId})"><i class="bi bi-plus"></i></button>
                </div>
                <span class="ms-3">${item.itemName}</span>
            </div>
            <div>₱${(item.itemPrice * item.quantity).toFixed(2)}</div>
        </div>
        `;
    });

    currentOrder.addons.forEach((addon) => {
        html += `
        <div class="modal-order-item">
            <div class="d-flex align-items-center">
                <div class="quantity-controls">
                    <button class="btn btn-sm btn-danger" onclick="decrementAddon(${addon.addonId})"><i class="bi bi-dash"></i></button>
                    <span class="mx-2">${addon.quantity}</span>
                    <button class="btn btn-sm btn-success" onclick="incrementAddon(${addon.addonId})"><i class="bi bi-plus"></i></button>
                </div>
                <span class="ms-3">${addon.addonName}</span>
            </div>
            <div>₱${(addon.price * addon.quantity).toFixed(2)}</div>
        </div>
        `;
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
    document.getElementById("itemsVariantsIds").value = currentOrder.items
        .map((i) => i.itemId)
        .join(",");
    document.getElementById("quantities").value = currentOrder.items
        .map((i) => i.quantity)
        .join(",");

    document.getElementById("addonIds").value = currentOrder.addons
        .map((a) => a.addonId)
        .join(",");
    document.getElementById("addonQuantities").value = currentOrder.addons
        .map((a) => a.quantity)
        .join(",");

    document.getElementById("order-form").submit();
}