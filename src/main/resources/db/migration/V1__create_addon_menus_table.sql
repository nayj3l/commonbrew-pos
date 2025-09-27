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
