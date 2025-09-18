document.addEventListener('DOMContentLoaded', function() {
    // Payment method selection
    const paymentMethods = document.querySelectorAll('.payment-method');
    const paymentMethodInput = document.getElementById('paymentMethodInput');
    const qrCarousel = document.getElementById('qrCarousel');
    
    paymentMethods.forEach(method => {
        method.addEventListener('click', function() {
            const methodValue = this.getAttribute('data-method');
            
            // Update selected payment method
            paymentMethodInput.value = methodValue;
            
            // Update UI
            paymentMethods.forEach(m => m.classList.remove('selected'));
            this.classList.add('selected');
            
            // Check if online payment is selected
            if (methodValue === 'online') {
                qrCarousel.style.display = 'block';
            } else {
                qrCarousel.style.display = 'none';
            }
        });
    });
    
    // Form submission handling
    const orderForm = document.getElementById('orderForm');
    
    orderForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const paymentMethod = paymentMethodInput.value;
        
        // Add additional validation if needed
        if (paymentMethod === 'online') {
            // You might want to add additional validation for online payment
            if (!confirm('Have you completed the QR code payment?')) {
                return;
            }
        }
        
        // Submit the form
        this.submit();
    });
    
    // Initialize the first payment method as selected
    if (paymentMethods.length > 0) {
        paymentMethods[0].classList.add('selected');
    }
});