-- ============================================================
-- REQUÊTES SQL : Panier lié à Catégorie et Sous-catégorie
-- ============================================================
-- Lien : carts (panier) → products → categories, subcategories
-- ============================================================

-- 1) Tous les paniers avec nom produit, catégorie et sous-catégorie
SELECT 
    c.id,
    c.customer_id,
    c.product_id,
    c.quantity,
    p.name AS nom_produit,
    p.price,
    cat.name AS categorie,
    sc.name AS sous_categorie
FROM carts c
JOIN products p ON p.id = c.product_id
LEFT JOIN categories cat ON cat.id = p.category_id
LEFT JOIN subcategories sc ON sc.id = p.subcategory_id
ORDER BY c.customer_id, cat.name, sc.name, p.name;


-- 2) Panier d'un client donné (ex: customer_id = 1) avec catégorie et sous-catégorie
SELECT 
    c.id AS panier_id,
    p.name AS nom_produit,
    cat.name AS categorie,
    sc.name AS sous_categorie,
    c.quantity,
    p.price,
    (c.quantity * p.price) AS total_ligne
FROM carts c
JOIN products p ON p.id = c.product_id
LEFT JOIN categories cat ON cat.id = p.category_id
LEFT JOIN subcategories sc ON sc.id = p.subcategory_id
WHERE c.customer_id = 1
ORDER BY cat.name, sc.name, p.name;


-- 3) Montant total par catégorie dans les paniers (tous clients)
SELECT 
    COALESCE(cat.name, 'Sans catégorie') AS categorie,
    COUNT(DISTINCT c.id) AS nb_lignes_panier,
    SUM(c.quantity) AS quantite_totale,
    SUM(c.quantity * p.price) AS montant_total
FROM carts c
JOIN products p ON p.id = c.product_id
LEFT JOIN categories cat ON cat.id = p.category_id
GROUP BY cat.id, cat.name
ORDER BY montant_total DESC;


-- 4) Montant total par sous-catégorie dans les paniers
SELECT 
    COALESCE(cat.name, 'Sans catégorie') AS categorie,
    COALESCE(sc.name, 'Sans sous-catégorie') AS sous_categorie,
    COUNT(DISTINCT c.id) AS nb_lignes_panier,
    SUM(c.quantity) AS quantite_totale,
    SUM(c.quantity * p.price) AS montant_total
FROM carts c
JOIN products p ON p.id = c.product_id
LEFT JOIN categories cat ON cat.id = p.category_id
LEFT JOIN subcategories sc ON sc.id = p.subcategory_id
GROUP BY cat.id, cat.name, sc.id, sc.name
ORDER BY cat.name, montant_total DESC;


-- 5) Détail panier par client avec catégorie / sous-catégorie (pour facture ou export)
SELECT 
    cust.prenom,
    cust.nom,
    cust.email,
    c.id AS ligne_panier_id,
    p.name AS produit,
    cat.name AS categorie,
    sc.name AS sous_categorie,
    c.quantity,
    p.price AS prix_unitaire,
    (c.quantity * p.price) AS total_ligne
FROM carts c
JOIN customers cust ON cust.id = c.customer_id
JOIN products p ON p.id = c.product_id
LEFT JOIN categories cat ON cat.id = p.category_id
LEFT JOIN subcategories sc ON sc.id = p.subcategory_id
ORDER BY cust.id, cat.name, sc.name, p.name;


-- ------------------------------------------------------------
-- 6) Vérifier que les tables ont les bonnes colonnes (à exécuter si erreur "Unknown column")
-- Products doit avoir : category_id, subcategory_id (optionnels)
-- ALTER TABLE products ADD COLUMN category_id BIGINT NULL;
-- ALTER TABLE products ADD COLUMN subcategory_id BIGINT NULL;
-- ALTER TABLE products ADD FOREIGN KEY (category_id) REFERENCES categories(id);
-- ALTER TABLE products ADD FOREIGN KEY (subcategory_id) REFERENCES subcategories(id);


-- ------------------------------------------------------------
-- 7) Vérifier le lien utilisateurs (CLIENT) ↔ clients (pour session panier)
-- Si customer_id ou c.user_id est NULL pour un user, "Session client non disponible" à la connexion.
SELECT 
    u.id AS user_id,
    u.username,
    c.id AS customer_id,
    c.prenom,
    c.nom,
    c.email,
    c.user_id
FROM users u
LEFT JOIN customers c ON c.user_id = u.id
WHERE u.role = 'CLIENT'
ORDER BY u.id
LIMIT 0, 25;
