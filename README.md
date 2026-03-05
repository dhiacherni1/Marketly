# E-Commerce Unifié

Application e-commerce JavaFX unifiée, regroupant les fonctionnalités des projets **aa1**, **ecommerce dhia**, **Connexion1ALL1** et **aziz** dans une seule interface moderne.

## Fonctionnalités

### Connexion / Inscription
- Écran de connexion avec redirection selon le rôle (Admin/Manager → Dashboard admin, Client → Espace client).
- Inscription (création utilisateur + client avec email `username@mail.com`).
- Gestion de session (SessionManager).

### Dashboard Admin (rôles ADMIN, MANAGER)
- **Catégories** : CRUD complet.
- **Clients** : CRUD complet.
- **Produits** : CRUD complet.
- **Fournisseurs** : CRUD complet.
- **Sous-catégories** : CRUD avec lien catégorie.
- **Utilisateurs** : CRUD (rôles ADMIN, MANAGER, CLIENT).
- **Statistiques** : Top produits, top clients, revenus par mois, graphiques (BarChart, LineChart).
- **Thème** : Bouton Clair/Sombre dans la barre supérieure.
- **Déconnexion**.

### Espace Client (rôle CLIENT)
- **Catégories** : Consultation en lecture seule.
- **Sous-catégories** : Consultation en lecture seule.
- **Produits** : Consultation, ajout au panier, chatbot ShopGuide (questions sur les produits).
- **Panier** : Modifier quantités, passer commande (checkout), facture PDF (OpenPDF).
- **Multi-langue** : Français, English, العربية (sélecteur en haut).
- **Déconnexion**.

## Prérequis

- **JDK 11** (ou 17 pour JavaFX 17).
- **Maven 3.6+**.
- **MySQL** : base `ecommerce_db` avec les tables : `users`, `customers`, `categories`, `subcategories`, `providers`, `products`, `carts`, `orders`, `order_items` (voir scripts SQL ci-dessous).

## Opérations sur la base de données ecommerce_db

| Fichier SQL | Rôle |
|-------------|------|
| **schema_ecommerce_db.sql** | Création complète de la base et de toutes les tables (à exécuter en premier si la base n’existe pas). |
| **migration_ajout_manquants.sql** | Ajoute les tables ou colonnes manquantes (carts, order_items, colonnes products, created_at). À exécuter si la base existe déjà mais est incomplète. |
| **requetes_panier_categorie.sql** | Requêtes métier (panier avec catégorie/sous-catégorie, totaux, etc.) — pour consultation ou rapports. |

**Ordre recommandé :**
1. **Nouvelle installation** : exécuter `schema_ecommerce_db.sql` (crée la base et les 9 tables).
2. **Base déjà partiellement créée** : exécuter `migration_ajout_manquants.sql` (ignorer les erreurs « Duplicate column » ou « Table already exists » si déjà appliqué).

Exemple en ligne de commande MySQL :
```bash
mysql -u root -p < schema_ecommerce_db.sql
```

## Configuration base de données

Dans `src/main/java/Utlis/DataSource.java`, vérifier l’URL, l’utilisateur et le mot de passe MySQL :

```java
private final String url = "jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private final String username = "root";
private final String password = "";
```

## Lancer l’application

### Avec Maven (ligne de commande)

```bash
cd C:\Users\pc\Desktop\ECommerceUnifie
mvn clean compile javafx:run
```

Ou pour créer un JAR exécutable (avec dépendances) :

```bash
mvn clean package
# Puis lancer avec Java en ajoutant le module path JavaFX si nécessaire.
```

### Avec IntelliJ IDEA

1. **Ouvrir le projet** : File → Open → sélectionner le dossier `ECommerceUnifie` (Maven importe le `pom.xml`).
2. **Configurer le JDK** : File → Project Structure → Project → SDK 11 (ou 17).
3. **Lancer** :
   - Soit utiliser la configuration d’exécution **MainAppFXML** (Run → Edit Configurations, ajouter une Application, main class : `Application.MainAppFXML` ; si besoin, VM options : `--module-path <chemin JavaFX> --add-modules javafx.controls,javafx.fxml`).
   - Soit exécuter la classe `Application.MainAppFXML` (clic droit → Run).

Si JavaFX n’est pas reconnu par le JDK, ajouter les bibliothèques JavaFX (par ex. depuis Maven) et les modules dans les VM options comme ci-dessus.

## Structure du projet

```
ECommerceUnifie/
├── pom.xml
├── src/main/java/
│   ├── Application/     # Contrôleurs JavaFX, MainAppFXML, SessionManager, ThemeManager, LanguageManager, etc.
│   ├── Entite/          # Category, Customer, Order, OrderItem, Panier, Product, Provider, SubCategory, User, stats
│   ├── Services/        # Services métier (CRUD, Order, Panier, Report)
│   └── Utlis/           # DataSource
├── src/main/resources/
│   ├── *.fxml           # Vues (login, register, main, client, onglets admin et client)
│   ├── styles.css
│   └── messages_fr.properties, messages_en.properties, messages_ar.properties
├── .idea/               # Config IntelliJ (misc, run configurations)
└── README.md
```

## Remerciements

Projet unifié à partir de :
- **aa1** : Panier client, commandes, facture PDF (OpenPDF), SessionManager.
- **ecommerce dhia** : Multi-langue (LanguageManager), panier/client, chatbot, ServiceUser avec authentification hashée.
- **Connexion1ALL1 (v2)** : ThemeManager, structure admin.
- **aziz** : Dashboard admin (CRUD, statistiques), inscription, Login avec redirection par rôle.
