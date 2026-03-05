-- ============================================================
-- SCHÉMA COMPLET : Base de données ecommerce_db
-- ============================================================
-- Crée la base et toutes les tables nécessaires à l'application
-- E-Commerce Unifié (admin, client, panier, commandes, factures).
-- Exécuter ce script en premier si la base n'existe pas encore.
-- ============================================================

CREATE DATABASE IF NOT EXISTS ecommerce_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE ecommerce_db;

-- ------------------------------------------------------------
-- 1) Utilisateurs (connexion admin / client)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CLIENT',
    created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME NULL,
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'MANAGER', 'CLIENT'))
);

-- ------------------------------------------------------------
-- 2) Clients (profil client, lié éventuellement à users)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prenom VARCHAR(100) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    user_id BIGINT NULL,
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- 3) Catégories
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL
);

-- ------------------------------------------------------------
-- 4) Sous-catégories (dépendent d'une catégorie)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS subcategories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category_id BIGINT NOT NULL,
    image_path VARCHAR(512) NULL,
    CONSTRAINT fk_subcategories_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- 5) Fournisseurs
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(50) NULL,
    address VARCHAR(255) NULL,
    email VARCHAR(255) NULL
);

-- ------------------------------------------------------------
-- 6) Produits (catégorie, sous-catégorie, fournisseur optionnels)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    price DOUBLE NOT NULL DEFAULT 0,
    quantity INT NOT NULL DEFAULT 0,
    category_id BIGINT NULL,
    subcategory_id BIGINT NULL,
    provider_id BIGINT NULL,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_products_subcategory FOREIGN KEY (subcategory_id) REFERENCES subcategories(id) ON DELETE SET NULL,
    CONSTRAINT fk_products_provider FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- 7) Panier (carts) — lignes panier par client
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
-- 8) Commandes (orders)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    total DOUBLE NOT NULL DEFAULT 0,
    order_date DATETIME NOT NULL,
    payment_method VARCHAR(50) NULL,
    address VARCHAR(255) NULL,
    phone VARCHAR(50) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- 9) Lignes de commande (order_items)
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

-- ============================================================
-- Fin du schéma. Pour ajouter des données de test ou des
-- colonnes supplémentaires, utiliser migration_ajout_manquants.sql
-- ou requetes_panier_categorie.sql pour des requêtes métier.
-- ============================================================
