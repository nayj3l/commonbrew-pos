let selectedVariant = null; 
let selectedItemName = "";

// Select Variant & Addons using cached data
function loadVariants(button) {
    console.log('=== loadVariants START ===');

    const variantQuantitiesDiv = document.getElementById('variantModalBody');
    variantQuantitiesDiv.innerHTML = '';

    const itemId = button.getAttribute("data-item-id");
    const menuId = button.getAttribute("data-menu-id");
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

        // Find menu by menuId
        const menu = orderData.menus.find(m => m.id == menuId);
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
        const variants = menu.variants || [];
        console.log('Menu variants:', variants);

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
        variants.forEach((variant) => {
            const wrapperHTML = `
                <div class="variant-item-row d-flex justify-content-between align-items-center border rounded p-2 mb-2">
                    <!-- LEFT panel (variant name + price + controls) -->
                    <div class="variant-item-label-wrapper d-flex flex-column">
                        <!-- Variant label (name + base price) -->
                        <div class="variant-item-label">${variant.variantName} (₱${variant.price.toFixed(2)})</div>
                        <!-- Controls row -->
                        <div class="d-flex align-items-center mt-1">
                            <button class="btn btn-sm btn-danger d-flex align-items-center 
                                    justify-content-center btn-minus">
                                <i class="bi bi-dash"></i>
                            </button>
                            <input type="number"
                                id="qty-${variant.variantId}"
                                value="0"
                                min="0"
                                class="form-control form-control-sm text-center mx-2 qty-input"
                                style="width:60px"
                                data-item-id="${itemId}"
                                data-variant-id="${variant.variantId}"
                                data-variant-name="${variant.variantName}"
                                data-price="${variant.price}"
                                data-variant-ad="${variant.variantAd}">
                            <button class="btn btn-sm btn-success d-flex align-items-center 
                                    justify-content-center btn-plus">
                                <i class="bi bi-plus"></i>
                            </button>
                        </div>
                    </div>
                    <!-- RIGHT panel (total price) -->
                    <div class="fw-bold text-end variant-total">₱0.00</div>
                </div>
            `;

            // Append to modal body
            modalBody.insertAdjacentHTML("beforeend", wrapperHTML);

            // Get the newly created elements
            const wrapper = modalBody.lastElementChild;
            const qtyInput = wrapper.querySelector('.qty-input');
            const plusBtn = wrapper.querySelector('.btn-plus');
            const minusBtn = wrapper.querySelector('.btn-minus');
            const totalLabel = wrapper.querySelector('.variant-total');

            // Update function
            const updateTotal = () => {
                const qty = parseInt(qtyInput.value) || 0;
                const total = variant.price * qty;
                totalLabel.textContent = `₱${total.toFixed(2)}`;
            };

            // Attach event listeners
            plusBtn.onclick = () => {
                qtyInput.value = parseInt(qtyInput.value) + 1;
                updateTotal();
                updateAddToOrderBtn();
            };

            minusBtn.onclick = () => {
                if (parseInt(qtyInput.value) > 0) {
                    qtyInput.value = parseInt(qtyInput.value) - 1;
                    updateTotal();
                    updateAddToOrderBtn();
                }
            };

            qtyInput.addEventListener('change', updateTotal);
            qtyInput.addEventListener('input', updateTotal);
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
                itemId: input.dataset.itemId,
                variantId: input.dataset.variantId,
                variantName: input.dataset.variantName,
                price: parseFloat(input.dataset.price),
                quantity: quantity,
            });
        }
    });
    
    return selectedVariants;
}

function updateAddons(itemId) {
    if (!itemId) return;

    // Reset addons array for this item only
    const item = currentOrder.items.find(i => i.itemId == itemId);
    if (!item) return;
    item.addons = [];

    document
        .querySelectorAll(`.addon-checkbox[data-item-id="${itemId}"]:checked`)
        .forEach((checkbox) => {
            const addonId = checkbox.value;
            const addonName = checkbox.getAttribute("data-addon-name");
            const addonPrice = parseFloat(checkbox.getAttribute("data-addon-price"));

            const existing = item.addons.find(a => a.addonId == addonId);
            if (existing) {
                existing.quantity += 1;
            } else {
                item.addons.push({
                    addonId,
                    addonName,
                    price: addonPrice,
                    quantity: 1,
                    itemId: itemId
                });
            }
        });

    calculateTotal();
}

function incrementItem(variantId) {
    const item = currentOrder.items.find((item) => item.variantId == variantId);
    if (item) {
        item.quantity += 1;
        calculateTotal();
        renderModalOrder();
    }
}

function decrementItem(variantId) {
    const itemIndex = currentOrder.items.findIndex(
        (item) => item.variantId == variantId
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

function incrementAddon(addonId) {
    const addon = currentOrder.addons.find(addon => addon.addonId === addonId);
    if (addon) {
        addon.quantity += 1;
        calculateTotal();
        renderModalOrder();
    } else {
        console.warn(`Addon with ID ${addonId} not found in current order`);
    }
}

function decrementAddon(addonId) {
    const addon = currentOrder.addons.find(addon => addon.addonId === addonId);
    
    if (addon) {
        if (addon.quantity > 1) {
            addon.quantity -= 1;
        } else {
            // Remove addon completely if quantity would become 0
            currentOrder.addons = currentOrder.addons.filter(a => a.addonId !== addonId);
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


// Helper: update Add to Order button text and disabled state
function updateAddToOrderBtn() {
    const modalBody = document.getElementById("variantModalBody");
    const qtyInputs = modalBody.querySelectorAll('.qty-input');
    const addToOrderBtn = document.getElementById("addVariantBtn");

    let total = 0;
    qtyInputs.forEach(input => {
        total += parseInt(input.value, 10) || 0;
    });

    console.log(total)
    addToOrderBtn.textContent = `Add to Order (${total}x)`;
    if (total == 0) {
        addToOrderBtn.textContent = `Add to Order`;
    }
}