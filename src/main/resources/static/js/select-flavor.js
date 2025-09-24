// Load menu items for a category
function loadItems(menuId) {
    console.log('=== loadItems START ===');
    console.log('menuId:', menuId, 'type:', typeof menuId);

    try {
        // Use cached data
        const orderData = loadOrderData();
        
        // Check if items exists and is an array
        if (!orderData.items || !Array.isArray(orderData.items)) {
            console.error('ERROR: orderData.items is not an array!');
            const selectFlavor = document.getElementById("itemsModalBody");
            if (selectFlavor) {
                selectFlavor.innerHTML = '<p class="text-center text-danger">Error: Items data is not available</p>';
            }
            return;
        }
        
        // Filter items belonging to the selected menu
        const items = orderData.items.filter(item => item.menuId == menuId);
        
        const selectFlavor = document.getElementById("itemsModalBody");
        
        if (!selectFlavor) {
            console.error('ERROR: itemsModalBody element not found!');
            return;
        }

        selectFlavor.innerHTML = "";

        if (!items || items.length === 0) {
            console.log('No items found for this menu');
            selectFlavor.innerHTML = '<p class="text-center">No items available in this category</p>';
            showModal();
            return;
        }

        items.forEach((item, index) => {
            const itemDiv = document.createElement("div");
            itemDiv.className = "mb-2";

            const flavorBtn = document.createElement("button");
            flavorBtn.className = "btn btn-outline-success item-btn";
            flavorBtn.textContent = item.name;
            flavorBtn.dataset.itemId = item.id;
            flavorBtn.dataset.itemPrice = item.basePrice;
            flavorBtn.onclick = function() {
                loadVariants(this);
                bootstrap.Modal.getInstance(document.getElementById("itemsModal")).hide();
            };

            itemDiv.appendChild(flavorBtn);
            selectFlavor.appendChild(itemDiv);
        });

        showModal();
        
    } catch (error) {
        console.error('=== ERROR in loadItems ===', error);
        console.error('Error stack:', error.stack);
    }
    console.log('=== loadItems END ===');
}

function showModal() {
    const modalElement = document.getElementById("itemsModal");
    if (!modalElement) {
        console.error('Modal element not found!');
        return;
    }
    
    try {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
    } catch (error) {
        console.error('Error showing modal:', error);
    }
}