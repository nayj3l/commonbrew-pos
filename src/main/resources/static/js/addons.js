function getSelectedVariants() {
    const selectedVariants = [];
    const quantityInputs = document.querySelectorAll('#variantQuantities input[type="number"]');
    
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

// Main function to add variants to order
function addVariantsWithAddons() {
    const selectedVariants = getSelectedVariants();
    const selectedAddons = updateSelectedAddons();
    
    // Check if at least one variant has quantity > 0
    if (selectedVariants.length === 0) {
        alert("Please select at least one variant with quantity greater than 0!");
        return;
    }
    
    // Add each variant to the order
    selectedVariants.forEach(variant => {
        // Check if this variant already exists in the order
        const existingIndex = currentOrder.items.findIndex(
            item => item.itemId === variant.variantId
        );
        
        if (existingIndex >= 0) {
            // Update existing variant quantity
            currentOrder.items[existingIndex].quantity += variant.quantity;
            
            // Merge addons
            selectedAddons.forEach(sa => {
                const existingAddon = currentOrder.items[existingIndex].addons.find(a => a.addonId === sa.addonId);
                if (existingAddon) {
                    existingAddon.quantity += sa.quantity;
                } else {
                    currentOrder.items[existingIndex].addons.push({...sa});
                }
            });
        } else {
            // Add new variant with addons
            currentOrder.items.push({
                itemId: variant.variantId,
                itemName: `${selectedItemName} (${variant.variantName})`,
                itemPrice: variant.price,
                quantity: variant.quantity,
                addons: selectedAddons.map(addon => ({...addon})) // Clone addons
            });
        }
    });
    
    calculateTotal();
    renderModalOrder();
    
    bootstrap.Modal.getInstance(document.getElementById("variantModal")).hide();
    resetSelections();
}

function resetSelections() {
    // Reset quantity inputs
    document.querySelectorAll('#variantQuantities input[type="number"]').forEach(input => {
        input.value = 0;
    });
    
    // Reset addon checkboxes
    document.querySelectorAll("#addonOptions input[type=checkbox]").forEach(cb => {
        cb.checked = false;
    });
    
    currentItemName = "";
}

document.querySelectorAll("#addonOptions > div").forEach(card => {
    card.addEventListener("click", function(e) {
        if (e.target.tagName === "INPUT") return;

        const checkbox = card.querySelector("input[type=checkbox]");
        checkbox.checked = !checkbox.checked;
        updateSelectedAddons();
    });
});
