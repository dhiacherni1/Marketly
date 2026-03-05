-- ============================================================
-- DONNÉES D'EXEMPLE : Panier avec produits, catégories et sous-catégories
-- ============================================================
-- À exécuter après schema_ecommerce_db.sql (base vide ou de test).
-- Insère catégories, sous-catégories, clients, produits puis des lignes de panier.
-- ============================================================

USE ecommerce_db;

-- Nettoyer les paniers et données de test (optionnel, décommenter si besoin)
-- DELETE FROM carts;
-- DELETE FROM products;
-- DELETE FROM subcategories;
-- DELETE FROM categories;
-- DELETE FROM customers;
-- DELETE FROM providers;

-- ------------------------------------------------------------
-- 1) Catégories
-- ------------------------------------------------------------
INSERT INTO categories (name, description) VALUES
('Électronique', 'Smartphones, ordinateurs et accessoires tech'),
('Mode', 'Vêtements et chaussures'),
('Maison', 'Meubles et décoration');

-- ------------------------------------------------------------
-- 2) Sous-catégories (rattachées aux catégories ci-dessus)
-- ------------------------------------------------------------
INSERT INTO subcategories (name, category_id) VALUES
('Smartphones', 1),
('Ordinateurs', 1),
('Accessoires', 1),
('Vêtements', 2),
('Chaussures', 2),
('Meuble', 3),
('Décoration', 3);

-- ------------------------------------------------------------
-- 3) Fournisseur (pour lier des produits)
-- ------------------------------------------------------------
INSERT INTO providers (name, phone, address) VALUES
('TechSupply', '01 23 45 67 89', '123 rue de la Tech, Paris'),
('Mode & Co', '01 98 76 54 32', '45 avenue de la Mode, Lyon');

-- ------------------------------------------------------------
-- 4) Clients (pour remplir le panier)
-- ------------------------------------------------------------

INSERT INTO customers (prenom, nom, email) VALUES
('Jean', 'Dupont', 'jean.dupont@mail.com'),
('Marie', 'Martin', 'marie.martin@mail.com');

-- ------------------------------------------------------------
-- 5) Produits avec catégorie et sous-catégorie
-- ------------------------------------------------------------
INSERT INTO products (name, price, quantity, category_id, subcategory_id, provider_id) VALUES
-- Électronique > Smartphones
('iPhone 15', 999.00, 50, 1, 1, 1),
('Samsung Galaxy S24', 899.00, 40, 1, 1, 1),
-- Électronique > Ordinateurs
('MacBook Pro 14"', 1999.00, 20, 1, 2, 1),
('PC Portable Gaming', 1299.00, 15, 1, 2, 1),
-- Électronique > Accessoires
('Clavier sans fil', 49.99, 100, 1, 3, 1),
('Souris ergonomique', 35.00, 80, 1, 3, 1),
-- Mode > Vêtements
('T-shirt Cotton', 29.90, 200, 2, 4, 2),
('Jean Slim', 79.90, 80, 2, 4, 2),
('Pull en laine', 59.00, 60, 2, 4, 2),
-- Mode > Chaussures
('Baskets running', 89.00, 45, 2, 5, 2),
('Bottes cuir', 149.00, 30, 2, 5, 2),
-- Maison > Meuble
('Étagère design', 129.00, 25, 3, 6, 2),
('Lampadaire moderne', 159.00, 20, 3, 6, 2),
-- Maison > Décoration
('Coussin décoratif', 39.00, 60, 3, 7, 2),
('Cadre photo A4', 24.90, 120, 3, 7, 2);

-- ------------------------------------------------------------
-- 6) Panier : lignes pour 2 clients avec des produits de toutes catégories
-- ------------------------------------------------------------
-- Client 1 (Jean Dupont) : électronique + mode + maison
-- Client 2 (Marie Martin) : mix catégories et sous-catégories

INSERT INTO carts (customer_id, product_id, quantity) VALUES
(1, 1, 2),   -- 2x iPhone 15 (Électronique > Smartphones)
(1, 3, 1),   -- 1x MacBook Pro (Électronique > Ordinateurs)
(1, 5, 3),   -- 3x Clavier (Électronique > Accessoires)
(1, 7, 2),   -- 2x T-shirt (Mode > Vêtements)
(1, 10, 1),  -- 1x Baskets (Mode > Chaussures)
(1, 12, 1),  -- 1x Étagère (Maison > Meuble)
(1, 14, 4),  -- 4x Coussin (Maison > Décoration)
(2, 2, 1),   -- 1x Samsung (Électronique > Smartphones)
(2, 4, 1),   -- 1x PC Gaming (Électronique > Ordinateurs)
(2, 6, 2),   -- 2x Souris (Électronique > Accessoires)
(2, 8, 1),   -- 1x Jean (Mode > Vêtements)
(2, 11, 1),  -- 1x Bottes (Mode > Chaussures)
(2, 13, 1),  -- 1x Lampadaire (Maison > Meuble)
(2, 15, 2);  -- 2x Cadre photo (Maison > Décoration)

-- ============================================================
-- Vérification : voir le panier avec catégories et sous-catégories
-- (requête similaire à requetes_panier_categorie.sql)
-- ============================================================
-- SELECT c.id, c.customer_id, p.name AS produit, cat.name AS categorie, sc.name AS sous_categorie, c.quantity, p.price, (c.quantity * p.price) AS total_ligne
-- FROM carts c
-- JOIN products p ON p.id = c.product_id
-- LEFT JOIN categories cat ON cat.id = p.category_id
-- LEFT JOIN subcategories sc ON sc.id = p.subcategory_id
-- ORDER BY c.customer_id, cat.name, sc.name;
