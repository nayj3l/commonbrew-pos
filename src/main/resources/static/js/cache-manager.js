const CACHE_KEY = 'orderPageData';
const CACHE_TTL = 1000 * 60 * 60; // 1 hour

function loadOrderData() {
    const cached = localStorage.getItem(CACHE_KEY);

    if (cached) {
        try {
            const parsed = JSON.parse(cached);
            const now = new Date().getTime();

            // Check if cache is still valid
            if (now - parsed.timestamp < CACHE_TTL) {
                // Ensure cached structure is correct
                if (parsed.data &&
                    Array.isArray(parsed.data.menus) &&
                    Array.isArray(parsed.data.items) &&
                    Array.isArray(parsed.data.variants) &&
                    Array.isArray(parsed.data.addons)
                ) {
                    console.log('Using cached order data');
                    return parsed.data;
                } else {
                    console.warn('Cached data structure is invalid, clearing cache.');
                    clearOrderCache();
                }
            } else {
                console.info('Cache expired, clearing cache.');
                clearOrderCache();
            }
        } catch (e) {
            console.error('Error parsing cached data, clearing cache.', e);
            clearOrderCache();
        }
    }

    // No valid cache -> use preloaded Thymeleaf JSON
    const data = {
        menus: window.preloadedMenus,
        items: window.preloadedItems,
        variants: window.preloadedVariants,
        addons: window.preloadedAddons
    };

    // Save freshly loaded data into cache
    localStorage.setItem(CACHE_KEY, JSON.stringify({
        timestamp: new Date().getTime(),
        data: data
    }));

    console.log('Cached fresh order data');
    return data;
}

function clearOrderCache() {
    localStorage.removeItem('orderPageData');
}
