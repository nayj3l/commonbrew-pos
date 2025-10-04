CREATE TABLE IF NOT EXISTS addon_menus (
  addon_id BIGINT NOT NULL,
  menu_id  BIGINT NOT NULL,
  PRIMARY KEY (addon_id, menu_id),
  CONSTRAINT fk_addon_menus_addon FOREIGN KEY (addon_id) REFERENCES addons(addon_id),
  CONSTRAINT fk_addon_menus_menu  FOREIGN KEY (menu_id)  REFERENCES menu(id)
) ENGINE=InnoDB;

ALTER TABLE menu ADD COLUMN image_url VARCHAR(500);

ALTER TABLE menu_items ADD COLUMN image_url VARCHAR(500);

ALTER TABLE addons ADD COLUMN image_thumbnail_url VARCHAR(500);

CREATE TABLE menu_menu_variant (
    menu_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    PRIMARY KEY (menu_id, variant_id),
    CONSTRAINT fk_menu
        FOREIGN KEY (menu_id) REFERENCES menu(id),
    CONSTRAINT fk_variant
        FOREIGN KEY (variant_id) REFERENCES menu_variants(variant_id)
);

ALTER TABLE order_item
ADD COLUMN item_id BIGINT NOT NULL;

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE order_item
ADD CONSTRAINT fk_orderitem_item
FOREIGN KEY (item_id) REFERENCES menu_items(id);

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE order_item
MODIFY COLUMN variant_id BIGINT NOT NULL;
