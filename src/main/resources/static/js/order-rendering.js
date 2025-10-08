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

    // Format currency (PH peso)
    const formatted = new Intl.NumberFormat('en-PH', {
        style: 'currency',
        currency: 'PHP'
    }).format(currentOrder.total);

    // Update total display
    const totalEl = document.getElementById("order-total");
    if (totalEl) totalEl.textContent = formatted;

    // Update cart message + color
    const noteEl = document.querySelector('.add-to-cart');
    if (noteEl) {
        if (total > 0) {
            noteEl.innerHTML = `<i class="bi bi-bag-check me-1"></i> Proceed to checkout`;
            noteEl.style.color = 'var(--success)';
        } else {
            noteEl.innerHTML = `<i class="bi bi-cart-x me-1"></i> There are no items in your cart`;
            noteEl.style.color = 'var(--bs-secondary-color)'
        }
    }

    // Optional: dispatch event for other listeners
    totalEl?.dispatchEvent(new CustomEvent('orderTotalChanged', { detail: { total } }));

    return total;
}


// Render order summary in modal
function renderModalOrder() {
    console.log("currentOrder:", currentOrder);

    console.log("=== CURRENT ORDER STATE ===");
    console.log("Items:", currentOrder.items);
    console.log("Addons:", currentOrder.addons);
    console.log("Total: ₱" + currentOrder.total.toFixed(2));
    console.log("Item count:", currentOrder.items.length);
    console.log("Addon count:", currentOrder.addons.length);
    console.log("========================");

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
                            <button class="btn btn-sm btn-danger" onclick="decrementItem(${item.variantId})"><i class="bi bi-dash"></i></button>
                            <span class="mx-2">${item.quantity}</span>
                            <button class="btn btn-sm btn-success" onclick="incrementItem(${item.variantId})"><i class="bi bi-plus"></i></button>
                        </div>
                        <div class="d-flex flex-grow-1 justify-content-between align-items-center">
                            <span class="ms-3">${item.itemName}</span>
                            <div>₱${(item.itemPrice * item.quantity).toFixed(2)}</div>
                        </div>
                    </div>
                </div>`;
    });

    // Show global addons
    currentOrder.addons.forEach(addon => {
        html += `<div class="modal-order-item">
                    <div class="d-flex align-items-center">
                        <div class="quantity-controls">
                            <button class="btn btn-sm btn-danger" onclick="decrementAddon(${addon.addonId})"><i class="bi bi-dash"></i></button>
                            <span class="mx-2">${addon.quantity}</span>
                            <button class="btn btn-sm btn-success" onclick="incrementAddon(${addon.addonId})"><i class="bi bi-plus"></i></button>
                        </div>
                        <div class="d-flex flex-grow-1 justify-content-between align-items-center">
                            <div class="d-flex flex-grow-1 justify-content-between align-items-center">
                                <span class="ms-3">${addon.addonName}</span>
                                <div>₱${(addon.price * addon.quantity).toFixed(2)}</div>
                            </div>
                        </div>
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
    console.log("=== SUBMIT CONFIRMED ORDER ===");
    console.log("Current order before clearing:", JSON.stringify(currentOrder, null, 2));

    // Save current order temporarily
    sessionStorage.setItem('pendingOrder', JSON.stringify(currentOrder));
    console.log("Stored current order in sessionStorage as 'pendingOrder'.");

    // Clear the current order
    currentOrder = { items: [], addons: [], total: 0 };
    console.log("Cleared currentOrder object in memory:", currentOrder);

    // Redirect to confirmation page
    console.log("Redirecting user to /order/confirm...");
    window.location.href = '/order/confirm';
}


