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

// Main function to add variants to order
function addVariantsWithAddons() {
    const selectedVariants = getSelectedVariants();
    const selectedAddons = updateSelectedAddons();
    
    if (selectedVariants.length === 0) {
        alert("Please select a variant");
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
        } else {
            currentOrder.items.push({
                itemId: variant.variantId,
                itemName: `${selectedItemName} (${variant.variantName})`,
                itemPrice: variant.price,
                quantity: variant.quantity,
                addons: []
            });
        }
    });

    // Add addons globally (once)
    selectedAddons.forEach(sa => {
        const existingAddon = currentOrder.addons.find(a => a.addonId === sa.addonId);
        if (existingAddon) {
            existingAddon.quantity += sa.quantity;
        } else {
            currentOrder.addons.push({...sa});
        }
    });


    calculateTotal();
    renderModalOrder();
    
    bootstrap.Modal.getInstance(document.getElementById("variantModal")).hide();
    resetSelections();
}

function resetSelections() {
    // ✅ Reset variant quantity inputs
    document.querySelectorAll('#variantQuantities input[type="number"]').forEach(input => {
        input.value = 0;
    });
    
    // ✅ Reset addon checkboxes
    document.querySelectorAll("#addonOptions input[type=checkbox]").forEach(cb => {
        cb.checked = false;
    });

    // ✅ Reset addon qty inputs
    document.querySelectorAll(".addon-qty").forEach(input => {
        input.value = 0;
    });

    // ✅ Reset addon totals
    document.querySelectorAll(".addon-total").forEach(totalDisplay => {
        totalDisplay.textContent = "₱0.00";
    });

    // ✅ Collapse addons if open
    const addonContainer = document.getElementById("addonOptions");
    if (!addonContainer.classList.contains("collapsed")) {
        toggleAddons();
    }

}


// Add event listener to the parent container that holds all addon cards
document.addEventListener('click', function(e) {
    const addonCard = e.target.closest('.addon-card');
    if (!addonCard) return;

    const addonId = addonCard.dataset.addonId;
    const checkbox = addonCard.querySelector('.addon-checkbox'); // first
    const price = parseFloat(checkbox.dataset.addonPrice) || 0;  // then use it
    const qtyControls = addonCard.querySelector('.addon-quantity-controls');
    const qtyInput = addonCard.querySelector('.addon-qty');
    const totalDisplay = addonCard.querySelector('.addon-total');

    // Handle plus button
    if (e.target.closest('.addon-plus')) {
        let currentQty = parseInt(qtyInput.value) || 0;
        currentQty++;
        
        qtyInput.value = currentQty;

        // ✅ Auto-check the checkbox when qty > 0
        checkbox.checked = currentQty > 0;

        updateAddonTotal(qtyInput, price, totalDisplay);
        if (checkbox.checked) updateSelectedAddons();
    }

    // Handle minus button
    if (e.target.closest('.addon-minus')) {
        let currentQty = parseInt(qtyInput.value) || 0;
        if (currentQty > 0) {
            currentQty--;
            qtyInput.value = currentQty;

            // Auto-uncheck if quantity is 0
            checkbox.checked = currentQty > 0;

            updateAddonTotal(qtyInput, price, totalDisplay);
            if (checkbox.checked) updateSelectedAddons();
        }
    }

    // Handle checkbox change to show/hide quantity controls
    if (e.target.classList.contains('addon-checkbox')) {
        if (checkbox.checked) {
            qtyControls.style.display = 'block';
            updateAddonTotal(qtyInput, price, totalDisplay);
        } else {
            qtyControls.style.display = 'none';
            qtyInput.value = 1; // Reset to 1 when unchecked
            updateAddonTotal(qtyInput, price, totalDisplay);
        }
    }
});

// Function to update addon total display
function updateAddonTotal(qtyInput, price, totalDisplay) {
    const qty = parseInt(qtyInput.value) || 0;
    const addonPrice = parseFloat(price) || 0;
    const total = addonPrice * qty;
    
    // Add green highlight effect
    totalDisplay.classList.add('updated');
    setTimeout(() => {
        totalDisplay.classList.remove('updated');
    }, 500); // remove after 0.5 seconds
    
    totalDisplay.textContent = `₱${total.toFixed(2)}`;
}

function updateSelectedAddons() {
    const selectedAddons = [];
    document.querySelectorAll('.addon-checkbox:checked').forEach(checkbox => {
        const addonCard = checkbox.closest('.addon-card');
        const qtyInput = addonCard.querySelector('.addon-qty');
        selectedAddons.push({
            addonId: parseInt(checkbox.value),
            addonName: addonCard.querySelector("span").textContent, // ⬅️ here
            quantity: parseInt(qtyInput.value) || 0,
            price: parseFloat(checkbox.dataset.addonPrice)
        });
    });
    
    console.log('Selected addons:', selectedAddons);
    return selectedAddons;
}

function toggleAddons() {
    const addonContainer = document.getElementById("addonOptions");
    const toggleIcon = document.getElementById("addon-toggle");

    addonContainer.classList.toggle("collapsed");
    toggleIcon.classList.toggle("collapsed");
}
