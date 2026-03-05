-- Migration: ajouter la colonne email à la table providers (bases existantes)
-- Exécuter ce script si votre base a été créée avant cette mise à jour.

ALTER TABLE providers ADD COLUMN IF NOT EXISTS email VARCHAR(255) NULL;

-- Si votre MySQL ne supporte pas IF NOT EXISTS pour ADD COLUMN, utilisez :
-- ALTER TABLE providers ADD COLUMN email VARCHAR(255) NULL;
