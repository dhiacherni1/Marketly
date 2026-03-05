-- ============================================================
-- Correction table orders : colonnes manquantes
-- Exécuter ce script une seule fois dans votre base ecommerce_db.
-- Si une ligne provoque "Duplicate column" ou "Nom du champ déjà utilisé", la colonne existe déjà : ignorer.
-- ============================================================

USE ecommerce_db;

-- Colonnes nécessaires pour la validation de commande (exécuter chaque ligne séparément si besoin)
ALTER TABLE orders ADD COLUMN total DOUBLE NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN payment_method VARCHAR(50) NULL;
ALTER TABLE orders ADD COLUMN address VARCHAR(255) NULL;
ALTER TABLE orders ADD COLUMN phone VARCHAR(50) NULL;
ALTER TABLE orders ADD COLUMN status VARCHAR(20) NULL DEFAULT 'PENDING';

-- Si erreur "Field 'order_number' doesn't have a default value" : autoriser NULL
-- (si la colonne est en INT, remplacer par : MODIFY COLUMN order_number INT NULL)
ALTER TABLE orders MODIFY COLUMN order_number VARCHAR(50) NULL;

-- Si erreur "Field 'total_amount' doesn't have a default value" : autoriser NULL ou défaut 0
ALTER TABLE orders MODIFY COLUMN total_amount DOUBLE NULL;
