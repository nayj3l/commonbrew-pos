let selectedVariant = null; 
let selectedItemName = "";

function addToOrder(button) {
    const itemId = button.getAttribute("data-item-id");
    selectedItemName = button.textContent;

    const modalBody = document.getElementById("variantModalBody");
    const addToOrderBtn = document.getElementById("addVariantBtn");

    modalBody.innerHTML = "<p class='text-center text-muted'>Loading variants...</p>";
    addToOrderBtn.style.display = "flex"; // show footer by default

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

// Add selected variant to order
document.getElementById("addVariantBtn").addEventListener("click", () => {
    if (!selectedVariant) {
        alert("Please select a variant!");
        return;
    }

    const quantity = parseInt("1");

    const existingItemIndex = currentOrder.items.findIndex(
        item => item.itemId == selectedVariant.variantId
    );

    if (existingItemIndex >= 0) {
        currentOrder.items[existingItemIndex].quantity += quantity;
    } else {
        currentOrder.items.push({
            itemId: selectedVariant.variantId,
            itemName: `${selectedItemName} (${selectedVariant.variantName})`,
            itemPrice: selectedVariant.price,
            quantity
        });
    }

    calculateTotal();
    renderOrder();

    const modalEl = document.getElementById("variantModal");
    bootstrap.Modal.getInstance(modalEl).hide();

    selectedVariant = null;
    selectedItemName = "";
});

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
function incrementItem(itemId) {
    const item = currentOrder.items.find((item) => item.itemId == itemId);
    if (item) {
        item.quantity += 1;
        calculateTotal();
        renderOrder();
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
