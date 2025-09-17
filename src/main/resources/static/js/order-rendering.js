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

// Render order summary
function renderOrder() {
    const panel = document.getElementById("order-summary");

    if (
        currentOrder.items.length === 0 &&
        currentOrder.addons.length === 0
    ) {
        panel.innerHTML =
            '<p class="text-center text-muted">No items yet</p>';
        return;
    }

    let html = "";

    currentOrder.items.forEach((item) => {
        html += `
        <div class="order-item">
            <div>
                <button class="btn btn-sm btn-danger ms-2" onclick="decrementItem(${item.itemId})"><i class="bi bi-dash"></i></button>
                <button class="btn btn-sm btn-success" onclick="incrementItem(${item.itemId})"><i class="bi bi-plus"></i></button>
                <span>${item.itemName} x${item.quantity}</span>
            </div>
            <div>₱${(item.itemPrice * item.quantity).toFixed(2)}</div>
        </div>
        `;
    });

    currentOrder.addons.forEach((addon) => {
        html += `
        <div class="order-item">
            <div>
                <button class="btn btn-sm btn-danger ms-2" onclick="decrementAddon(${addon.addonId})"><i class="bi bi-dash"></i></button>
                <button class="btn btn-sm btn-success" onclick="incrementAddon(${addon.addonId})"><i class="bi bi-plus"></i></button>
                <span>${addon.addonName} x${addon.quantity}</span>
            </div>
            <div>₱${(addon.price * addon.quantity).toFixed(2)}</div>
        </div>
        `;
    });

    panel.innerHTML = html;
}

// Show confirmation modal
function confirmOrder() {
    if (
        currentOrder.items.length === 0 &&
        currentOrder.addons.length === 0
    ) {
        alert("No items in the order!");
        return;
    }

    let html = "<ul class='list-group'>";
    currentOrder.items.forEach((item) => {
        html += `<li class="list-group-item d-flex justify-content-between">
                <span>${item.itemName} x${item.quantity}</span>
                <span>₱${(item.itemPrice * item.quantity).toFixed(2)}</span>
            </li>`;
    });
    currentOrder.addons.forEach((addon) => {
        html += `<li class="list-group-item d-flex justify-content-between">
                <span>${addon.addonName} x${addon.quantity}</span>
                <span>₱${(addon.price * addon.quantity).toFixed(2)}</span>
            </li>`;
    });
    html += "</ul>";

    document.getElementById("modal-order-summary").innerHTML = html;
    document.getElementById("modal-order-total").textContent =
        currentOrder.total.toFixed(2);

    let modal = new bootstrap.Modal(
        document.getElementById("orderConfirmModal")
    );
    modal.show();
}

// Submit the confirmed order
function submitConfirmedOrder() {
    document.getElementById("itemIds").value = currentOrder.items
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