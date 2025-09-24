let selectedVariant = null; 
let selectedItemName = "";

// Select Variant & Addons using cached data
function loadVariants(button) {
    console.log('=== loadVariants START ===');
    
    const variantQuantitiesDiv = document.getElementById('variantModalBody');
    variantQuantitiesDiv.innerHTML = '';

    const itemId = button.getAttribute("data-item-id");
    selectedItemName = button.textContent;

    const modalBody = document.getElementById("variantModalBody");
    const addToOrderBtn = document.getElementById("addVariantBtn");

    modalBody.innerHTML = "<p class='text-center text-muted'>Loading variants...</p>";
    addToOrderBtn.style.display = "flex";

    const modal = new bootstrap.Modal(document.getElementById("variantModal"));
    modal.show();

    try {
        // Use cached data
        const orderData = loadOrderData();

        console.log('Cached data loaded:', orderData);

        // Convert itemId to number for comparison
        const numericItemId = Number(itemId);
        
        // Find the item in cached data
        const item = orderData.items.find(item => item.id === numericItemId);
        console.log('Found item:', item);

        if (!item) {
            modalBody.innerHTML = "<p class='text-center text-danger'>Item not found in cached data.</p>";
            selectedVariant = null;
            addToOrderBtn.style.display = "none";
            return;
        }

        // Use the variants from the item (they're already in the item object)
        const variants = item.variants || [];
        console.log('Item variants:', variants);

        modalBody.innerHTML = ""; // clear loading message

        if (!variants || variants.length === 0) {
            modalBody.innerHTML = "<p class='text-center'>No variants available for this item.</p>";
            selectedVariant = null; 
            addToOrderBtn.style.display = "none";
            return;
        }

        // Reset the "Add to Order" button
        addToOrderBtn.style.display = "flex";

        // Display variant buttons
        variants.forEach(variant => {
            const wrapper = document.createElement("div");
            wrapper.className = "d-flex justify-content-between align-items-center border rounded p-2 mb-2";

            // LEFT panel (variant name + price + controls)
            const leftPanel = document.createElement("div");
            leftPanel.className = "d-flex flex-column";

            // Variant label (name + base price)
            const label = document.createElement("div");
            label.textContent = `${variant.variantName} (₱${variant.price.toFixed(2)})`;

            // Controls row
            const controls = document.createElement("div");
            controls.className = "d-flex align-items-center mt-1";

            const minusBtn = document.createElement("button");
            minusBtn.className = "btn btn-sm btn-danger d-flex align-items-center justify-content-center";
            minusBtn.innerHTML = '<i class="bi bi-dash"></i>';

            const qtyInput = document.createElement("input");
            qtyInput.type = "number";
            qtyInput.id = `qty-${variant.variantId}`;
            qtyInput.value = 0;
            qtyInput.min = 0;
            qtyInput.className = "form-control form-control-sm text-center mx-2";
            qtyInput.style.width = "60px";

            qtyInput.dataset.variantId = variant.variantId;
            qtyInput.dataset.variantName = variant.variantName;
            qtyInput.dataset.price = variant.price;
            qtyInput.dataset.variantAd = variant.variantAd;

            const plusBtn = document.createElement("button");
            plusBtn.className = "btn btn-sm btn-success d-flex align-items-center justify-content-center";
            plusBtn.innerHTML = '<i class="bi bi-plus"></i>';

            controls.appendChild(minusBtn);
            controls.appendChild(qtyInput);
            controls.appendChild(plusBtn);

            leftPanel.appendChild(label);
            leftPanel.appendChild(controls);

            // RIGHT panel (total price)
            const totalLabel = document.createElement("div");
            totalLabel.className = "fw-bold text-end variant-total";
            totalLabel.textContent = "₱0.00";

            // Update function
            const updateTotal = () => {
                const qty = parseInt(qtyInput.value) || 0;
                const total = variant.price * qty;
                totalLabel.textContent = `₱${total.toFixed(2)}`;
            };

            plusBtn.onclick = () => {
                qtyInput.value = parseInt(qtyInput.value) + 1;
                updateTotal();
            };

            minusBtn.onclick = () => {
                if (parseInt(qtyInput.value) > 0) {
                    qtyInput.value = parseInt(qtyInput.value) - 1;
                    updateTotal();
                }
            };

            qtyInput.addEventListener('change', updateTotal);
            qtyInput.addEventListener('input', updateTotal);

            wrapper.appendChild(leftPanel);
            wrapper.appendChild(totalLabel);

            modalBody.appendChild(wrapper);
        });

        console.log('=== loadVariants END ===');

    } catch (error) {
        console.error('Error in loadVariants:', error);
        modalBody.innerHTML = "<p class='text-center text-danger'>Error loading variants from cache. Please try again.</p>";
    }
}

// The rest of your functions remain the same...
function getSelectedVariants() {
    const selectedVariants = [];
    const quantityInputs = document.querySelectorAll('#variantModalBody input[type="number"]');
    
    quantityInputs.forEach(input => {
        const quantity = parseInt(input.value);
        if (quantity > 0) {
            selectedVariants.push({
                variantId: input.getAttribute('data-variant-id'),
                variantName: input.getAttribute('data-variant-name'),
                price: parseFloat(input.getAttribute('data-price')),
                quantity: quantity
            });
        }
    });
    
    return selectedVariants;
}

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
}

function incrementItem(itemId) {
    const item = currentOrder.items.find((item) => item.itemId == itemId);
    if (item) {
        item.quantity += 1;
        calculateTotal();
        renderModalOrder();
    }
}

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

function decrementAddon(itemId, addonId) {
    const item = currentOrder.items.find(i => i.itemId == itemId);
    if (!item || !item.addons) return;

    const addonIndex = item.addons.findIndex(a => a.addonId == addonId);
    if (addonIndex >= 0) {
        const addon = item.addons[addonIndex];
        if (addon.quantity > 1) {
            addon.quantity -= 1;
        } else {
            item.addons.splice(addonIndex, 1);
        }
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
        
        if (item.addons && item.addons.length > 0) {
            item.addons.forEach((addon) => {
                html += `
                <div class="order-addon d-flex justify-content-between align-items-center mb-1 ms-4 text-muted">
                    <div class="d-flex align-items-center">
                        <button class="btn btn-sm btn-outline-danger btn-sm" onclick="decrementAddon('${item.itemId}', '${addon.addonId}'); renderOrderModal();">
                            <i class="bi bi-dash"></i>
                        </button>
                        <span class="mx-1">${addon.quantity}</span>
                        <button class="btn btn-sm btn-outline-success btn-sm" onclick="incrementAddon('${item.itemId}', '${addon.addonId}'); renderOrderModal();">
                            <i class="bi bi-plus"></i>
                        </button>
                        <span class="ms-2 small">+ ${addon.addonName}</span>
                    </div>
                    <div class="small">₱${(addon.price * addon.quantity).toFixed(2)}</div>
                </div>
                `;
            });
        }
    });

    panel.innerHTML = html;
    calculateTotal();
    document.getElementById("modal-order-total").textContent = currentOrder.total.toFixed(2);
}
