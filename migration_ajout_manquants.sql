-- ============================================================
-- MIGRATION : Ajouter ce qui manque dans ecommerce_db
-- Exécuter dans l'ordre. En cas d'erreur "Duplicate column",
-- la colonne existe déjà : ignorer cette ligne.
-- ============================================================

USE ecommerce_db;

-- ------------------------------------------------------------
-- 0b) Lien client ↔ utilisateur (pour session panier à la connexion)
-- ------------------------------------------------------------
ALTER TABLE customers ADD COLUMN user_id BIGINT NULL;
ALTER TABLE customers ADD CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- ------------------------------------------------------------
-- 0) Table carts (panier) — obligatoire pour panier client et checkout
--    Sans cette table, panier et passage de commande ne fonctionnent pas.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_carts_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- 1) Table order_items (OBLIGATOIRE pour commandes + facture PDF)
--    Sans cette table, le passage de commande depuis le panier échoue.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    line_total DOUBLE NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ------------------------------------------------------------
-- 2) Colonnes dans products pour lier catégorie et sous-catégorie
--    (requêtes panier ↔ catégorie / sous-catégorie)
-- ------------------------------------------------------------
ALTER TABLE products ADD COLUMN category_id BIGINT NULL;
ALTER TABLE products ADD COLUMN subcategory_id BIGINT NULL;
ALTER TABLE products ADD COLUMN provider_id BIGINT NULL;

-- Clés étrangères (optionnel, pour intégrité)
ALTER TABLE products ADD CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id);
ALTER TABLE products ADD CONSTRAINT fk_products_subcategory FOREIGN KEY (subcategory_id) REFERENCES subcategories(id);
ALTER TABLE products ADD CONSTRAINT fk_products_provider FOREIGN KEY (provider_id) REFERENCES providers(id);

-- ------------------------------------------------------------
-- 3) Optionnel : created_at sur carts (pour stats "revenus par mois")
-- ------------------------------------------------------------
ALTER TABLE carts ADD COLUMN created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP;

-- ------------------------------------------------------------
-- 4) Sous-catégories : image bannière (Connexion1ALL1)
-- ------------------------------------------------------------
ALTER TABLE subcategories ADD COLUMN image_path VARCHAR(512) NULL;

-- ------------------------------------------------------------
-- 5) Utilisateurs : dates pour dashboard (nouveaux inscrits, actifs)
-- ------------------------------------------------------------
ALTER TABLE users ADD COLUMN created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN last_login DATETIME NULL;

-- ------------------------------------------------------------
-- 6) Commandes : colonnes total + statut (obligatoires pour validation)
--    "Unknown column 'total' in 'field list'" = exécuter la ligne total ci-dessous
-- ------------------------------------------------------------
ALTER TABLE orders ADD COLUMN total DOUBLE NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN status VARCHAR(20) NULL DEFAULT 'PENDING';
