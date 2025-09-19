function updateSelectedAddons() {
    // store selected addons temporarily for the item being added
    const selectedAddons = [];
    document.querySelectorAll("#addonOptions input:checked").forEach(cb => {
        selectedAddons.push({
            addonId: parseInt(cb.value),
            addonName: cb.nextElementSibling.querySelector("span").textContent,
            price: parseFloat(cb.getAttribute("data-addon-price")),
            quantity: 1
        });
    });
    return selectedAddons;
}

function addVariantWithAddons() {
    if (!selectedVariant) {
        alert("Please select a variant!");
        return;
    }

    const quantity = 1;
    const selectedAddons = updateSelectedAddons();

    const existingIndex = currentOrder.items.findIndex(
        i => i.itemId === selectedVariant.variantId
    );

    if (existingIndex >= 0) {
        currentOrder.items[existingIndex].quantity += quantity;

        // merge addons
        selectedAddons.forEach(sa => {
            const existingAddon = currentOrder.items[existingIndex].addons.find(a => a.addonId === sa.addonId);
            if (existingAddon) existingAddon.quantity += sa.quantity;
            else currentOrder.items[existingIndex].addons.push(sa);
        });
    } else {
        currentOrder.items.push({
            itemId: selectedVariant.variantId,
            itemName: `${selectedItemName} (${selectedVariant.variantName})`,
            itemPrice: selectedVariant.price,
            quantity,
            addons: selectedAddons
        });
    }

    calculateTotal();
    renderModalOrder();

    // auto-close modal
    bootstrap.Modal.getInstance(document.getElementById("variantModal")).hide();
    selectedVariant = null;
    selectedItemName = "";
}
