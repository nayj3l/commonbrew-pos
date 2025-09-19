let selectedVariant = null; 
let selectedItemName = "";

function addToOrder(button) {
    const itemId = button.getAttribute("data-item-id");
    selectedItemName = button.textContent;

    const modalBody = document.getElementById("variantModalBody");
    const addToOrderBtn = document.getElementById("addVariantBtn");

    modalBody.innerHTML = "<p class='text-center text-muted'>Loading variants...</p>";
    addToOrderBtn.style.display = "flex";

    const modal = new bootstrap.Modal(document.getElementById("variantModal"));
    modal.show();

    fetch(`/order/items/${itemId}/variants`)
        .then(res => res.json())
        .then(variants => {
            modalBody.innerHTML = ""; // clear loading message

            if (!variants || variants.length === 0) {
                // no variants available
                modalBody.innerHTML = "<p class='text-center'>No variants available for this item.</p>";
                selectedVariant = null; 
                addToOrderBtn.style.display = "none";
                return;
            }

            // Display variant buttons
            variants.forEach(variant => {
                const btn = document.createElement("button");
                btn.className = "btn btn-outline-primary m-1";
                btn.textContent = `${variant.variantName} - ₱${variant.price.toFixed(2)}`;
                btn.onclick = () => {
                    selectedVariant = variant;
                    modalBody.querySelectorAll("button").forEach(b => b.classList.remove("active"));
                    btn.classList.add("active");
                };
                modalBody.appendChild(btn);
            });
        })
        .catch(err => {
            console.error(err);
            modalBody.innerHTML = "<p class='text-center text-danger'>Error loading variants. Please try again.</p>";
        });
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
    // renderOrder();
}

// Increment item quantity
function incrementItem(itemId) {
    const item = currentOrder.items.find((item) => item.itemId == itemId);
    if (item) {
        item.quantity += 1;
        calculateTotal();
        renderModalOrder();
    }
}

// Decrement item quantity
function decrementItem(itemId) {
    const itemIndex = currentOrder.items.findIndex(
        (item) => item.itemId == itemId
    );
    if (itemIndex >= 0) {
        const item = currentOrder.items[itemIndex];
        if (item.quantity > 1) {
            item.quantity -= 1;
        } else {
            currentOrder.items.splice(itemIndex, 1);
        }
        calculateTotal();
        renderModalOrder();
    }
}

// Increment addon quantity
function incrementAddon(itemId, addonId) {
    const item = currentOrder.items.find(i => i.itemId == itemId);
    if (!item || !item.addons) return;

    const addon = item.addons.find(a => a.addonId == addonId);
    if (addon) {
        addon.quantity += 1;
        calculateTotal();
        renderModalOrder();
    }
}

// Decrement addon quantity
function decrementAddon(itemId, addonId) {
    const item = currentOrder.items.find(i => i.itemId == itemId);
    if (!item || !item.addons) return;

    const addonIndex = item.addons.findIndex(a => a.addonId == addonId);
    if (addonIndex >= 0) {
        const addon = item.addons[addonIndex];
        if (addon.quantity > 1) {
            addon.quantity -= 1;
        } else {
            // Remove only the addon, not the item
            item.addons.splice(addonIndex, 1);
        }
        // IMPORTANT: do NOT remove the item just because addons is empty
        calculateTotal();
        renderModalOrder();
    }
}


function renderOrderModal() {
    const panel = document.getElementById("modal-order-summary");
    if (currentOrder.items.length === 0 && currentOrder.addons.length === 0) {
        panel.innerHTML = '<p class="text-center text-muted">No items yet</p>';
        document.getElementById("modal-order-total").textContent = "0.00";
        return;
    }

    let html = "";

    currentOrder.items.forEach((item) => {
        html += `
        <div class="order-item d-flex justify-content-between align-items-center mb-2">
            <div>
                <button class="btn btn-sm btn-danger ms-2" onclick="decrementItem(${item.itemId}); renderOrderModal();">
                    <i class="bi bi-dash"></i>
                </button>
                <button class="btn btn-sm btn-success" onclick="incrementItem(${item.itemId}); renderOrderModal();">
                    <i class="bi bi-plus"></i>
                </button>
                <span>${item.itemName} x<span id="item-qty-${item.itemId}">${item.quantity}</span></span>
            </div>
            <div>₱<span id="item-total-${item.itemId}">${(item.itemPrice * item.quantity).toFixed(2)}</span></div>
        </div>
        `;
    });

    currentOrder.addons.forEach((addon) => {
        html += `
        <div class="order-item d-flex justify-content-between align-items-center mb-2">
            <div>
                <button class="btn btn-sm btn-danger ms-2" onclick="decrementAddon(${addon.addonId}); renderOrderModal();">
                    <i class="bi bi-dash"></i>
                </button>
                <button class="btn btn-sm btn-success" onclick="incrementAddon(${addon.addonId}); renderOrderModal();">
                    <i class="bi bi-plus"></i>
                </button>
                <span>${addon.addonName} x<span id="addon-qty-${addon.addonId}">${addon.quantity}</span></span>
            </div>
            <div>₱<span id="addon-total-${addon.addonId}">${(addon.price * addon.quantity).toFixed(2)}</span></div>
        </div>
        `;
    });

    calculateTotal(); // recalc total
    document.getElementById("modal-order-total").textContent = currentOrder.total.toFixed(2);
}

