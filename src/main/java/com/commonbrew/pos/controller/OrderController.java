package com.commonbrew.pos.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.commonbrew.pos.constants.PaymentOption;
import com.commonbrew.pos.model.Addon;
import com.commonbrew.pos.model.Menu;
import com.commonbrew.pos.model.MenuItem;
import com.commonbrew.pos.model.Order;
import com.commonbrew.pos.model.dto.AddonConfirmSummary;
import com.commonbrew.pos.model.dto.ItemVariantDto;
import com.commonbrew.pos.model.dto.MenuItemDto;
import com.commonbrew.pos.model.dto.OrderConfirmSummary;
import com.commonbrew.pos.model.dto.OrderConfirmSummaryResponse;
import com.commonbrew.pos.service.AddonService;
import com.commonbrew.pos.service.MenuItemService;
import com.commonbrew.pos.service.MenuService;
import com.commonbrew.pos.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final MenuItemService itemService;
    private final AddonService addonService;
    private final OrderService orderService;
    private final MenuService menuService;

    @GetMapping
    public String showOrderPage(Model model) {
        List<Menu> menu = menuService.getAllMenu();
        List<MenuItem> menuItems = itemService.getAllItems();
        List<Addon> addons = addonService.getAllAddons();

        model.addAttribute("menu", menu);
        model.addAttribute("items", menuItems);
        model.addAttribute("addons", addons);

        return "order";
    }

    @GetMapping("/items/{itemId}/variants")
    @ResponseBody
    public List<ItemVariantDto> getVariantsByMenuItem(@PathVariable Long itemId) {
        MenuItem item = itemService.getItemById(itemId);

        return item.getVariants().stream()
            .map(v -> new ItemVariantDto(v.getVariantId(), v.getMenuItem().getId(), v.getVariantName(), v.getPrice()))
            .collect(Collectors.toList());
    }

    @GetMapping("/items/{menuId}")
    @ResponseBody
    public List<MenuItemDto> getItemsByCategory(@PathVariable Long menuId) {
        List<MenuItem> items = itemService.getMenuItemsByMenuId(menuId);
        return items.stream()
                .map(item -> new MenuItemDto(
                        item.getId(),
                        item.getName()))
                .collect(Collectors.toList());
    }

    @PostMapping("/preview")
    @ResponseBody
    public OrderConfirmSummary previewOrder(
            @RequestParam("itemIds") List<Long> itemIds,
            @RequestParam("quantities") List<Integer> quantities,
            @RequestParam(value = "variantIds", required = false) List<Long> variantIds,
            @RequestParam(value = "itemAddonCsv", required = false) List<String> itemAddonCsv,
            @RequestParam(value = "addonIds", required = false) List<Long> orderAddonIds,
            @RequestParam(value = "addonQuantities", required = false) List<Integer> addonQuantities) {

        if (itemIds == null || quantities == null || itemIds.size() != quantities.size()) {
            throw new IllegalArgumentException("itemIds and quantities are required and must have the same length");
        }

        List<List<Long>> itemAddonIds = parseItemAddonCsv(itemAddonCsv, itemIds.size());

        // return orderService.buildOrderSummary(
        //         itemIds,
        //         quantities,
        //         variantIds,
        //         itemAddonIds,
        //         orderAddonIds,
        //         addonQuantities
        // );
        return null;
    }

    // Final order submission (redirect to success page)
    @PostMapping("/submit")
    public String submitOrder(
            @RequestParam("variantId") List<Long> variantIds,
            @RequestParam("quantity") List<Integer> quantities,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "unpaidReason", required = false) String unpaidReason,
            RedirectAttributes redirectAttributes) {

        if (variantIds == null || quantities == null || variantIds.size() != quantities.size()) {
            throw new IllegalArgumentException("variantIds and quantities are required and must have the same length");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String barista = authentication.getName();

        // List<List<Long>> itemAddonIds = parseItemAddonCsv(itemAddonCsv, itemIds.size());

        PaymentOption paymentOption = PaymentOption.valueOf(paymentMethod.toUpperCase());

        Order savedOrder = orderService.createOrder(
                variantIds,
                quantities,
                paymentOption,
                unpaidReason,
                barista
        );
        redirectAttributes.addAttribute("orderId", savedOrder.getId());
        redirectAttributes.addAttribute("totalAmount", savedOrder.getTotalAmount());

        return "redirect:/order/success";
    }

    @GetMapping("/success")
    public String showSuccessPage(@RequestParam Long orderId,
                                @RequestParam Double totalAmount,
                                Model model) {
        
        Order order = orderService.findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Order not found"));

        model.addAttribute("orderId", order.getId());
        model.addAttribute("totalAmount", order.getTotalAmount());
        return "order-success";
    }

    /**
     * Handles order confirmation request.
     *
     * @param itemIds        IDs of the ordered items.
     * @param quantities     Quantities corresponding to each item.
     * @param variantIds     (Optional) Variant IDs for each line item. Can be null.
     * @param itemAddonCsv   (Optional) Per-item addons as CSV strings. 
     *                       Example: ["5,6", "", "3"] means:
     *                       - first item has addons 5 and 6
     *                       - second item has none
     *                       - third item has addon 3
     * @param orderAddonIds  (Optional) Addons applied at the order level.
     * @param addonQuantities (Optional) Quantities for each order-level addon.
     * @param model          Spring MVC model for passing attributes to the view.
     * @return View name for the confirmation page.
     */
    @PostMapping("/confirm")
    public String confirmOrder(
            @RequestParam("itemsVariantsIds") List<Integer> itemsVariantsIds,
            @RequestParam("quantities") List<Integer> quantities,
            @RequestParam(value = "addonItemIds", required = false) List<Integer> addonItemIds,
            @RequestParam(value = "addonIds", required = false) List<Integer> addonIds,
            @RequestParam(value = "addonQuantities", required = false) List<Integer> addonQuantities,
            Model model) {
                
        log.info("Received itemIds: {}", itemsVariantsIds);
        log.info("Received quantities: {}", quantities);

        if (addonItemIds == null) {
            addonItemIds = new ArrayList<>();
        }
        if (addonIds == null) {
            addonIds = new ArrayList<>();
        }
        if (addonQuantities == null) {
            addonQuantities = new ArrayList<>();
        }

        if (itemsVariantsIds == null || quantities == null || itemsVariantsIds.size() != quantities.size()) {
            throw new IllegalArgumentException("itemIds and quantities are required and must have the same length");
        }

        List<OrderConfirmSummary> items = orderService.buildOrderSummary(itemsVariantsIds, quantities);
        List<AddonConfirmSummary> addons = new ArrayList<>();
        if (addonIds != null) {
            for (int i = 0; i < addonIds.size(); i++) {
                int itemId = addonItemIds.get(i);
                int addonId = addonIds.get(i);
                int quantity = addonQuantities.get(i);

                Addon addon = orderService.getAddonById(Long.valueOf(addonId));
                addons.add(new AddonConfirmSummary(
                    addon.getAddonName(),                  // Use addon name here
                    (long) itemId,                         // parent item id is okay
                    (long) addonId,
                    "-",                                   // variant name placeholder
                    quantity,
                    BigDecimal.valueOf(addon.getPrice()),
                    BigDecimal.valueOf(addon.getPrice() * quantity)
                ));
            }
        }

        BigDecimal total = items.stream()
            .map(OrderConfirmSummary::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .add(addons.stream().map(AddonConfirmSummary::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add));

        model.addAttribute("orderConfirmSummary", new OrderConfirmSummaryResponse(items, addons, total));
        return "order-confirm";
    }

    /**
     * Helper: convert list of CSV strings into List<List<Long>> with same size as itemsCount.
     * If itemAddonCsv is null, returns a list of nulls to indicate "no per-item addons".
     */
    private List<List<Long>> parseItemAddonCsv(List<String> itemAddonCsv, int itemsCount) {
        if (itemAddonCsv == null) {
            // keep it null for the service (service accepts null)
            return null;
        }

        List<List<Long>> parsed = new ArrayList<>(itemsCount);
        for (int i = 0; i < itemsCount; i++) {
            if (i < itemAddonCsv.size() && itemAddonCsv.get(i) != null && !itemAddonCsv.get(i).trim().isEmpty()) {
                String csv = itemAddonCsv.get(i).trim();
                String[] parts = csv.split(",");
                List<Long> ids = new ArrayList<>();
                for (String p : parts) {
                    String s = p.trim();
                    if (!s.isEmpty()) {
                        try {
                            ids.add(Long.parseLong(s));
                        } catch (NumberFormatException ex) {
                            // ignore invalid tokens (or throw if you prefer strict)
                        }
                    }
                }
                parsed.add(ids);
            } else {
                parsed.add(null); // no addons for this item
            }
        }
        return parsed;
    }


    // Show past orders
    @GetMapping("/history")
    public String showOrderHistory(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "order-history";
    }

}
