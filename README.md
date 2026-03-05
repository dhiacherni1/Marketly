# Marketly E-commerce

Application e-commerce desktop **JavaFX** (Java 8) pour **Marketly** : interface admin (dashboard, CRUD, statistiques) et espace client (boutique, panier, commandes, facture PDF), avec multi-langue (FR/EN/AR) et thème clair/sombre.

## Fonctionnalités

### Connexion / Inscription
- Écran de connexion avec redirection selon le rôle (Admin/Manager → Dashboard admin, Client → Espace client).
- Inscription (création utilisateur + client avec email).
- Gestion de session (`SessionManager`).

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
- **Catégories / Sous-catégories** : Consultation en lecture seule.
- **Produits** : Consultation, ajout au panier, chatbot ShopGuide (questions sur les produits).
- **Panier** : Modifier quantités, passer commande (checkout), facture PDF (OpenPDF).
- **Multi-langue** : Français, English, العربية (sélecteur en haut).
- **Déconnexion**.

## Prérequis

- **JDK 8** (ou 11+ ; JavaFX fourni via Maven).
- **Maven 3.6+**.
- **MySQL** : base `ecommerce_db` avec les tables : `users`, `customers`, `categories`, `subcategories`, `providers`, `products`, `carts`, `orders`, `order_items` (voir scripts SQL ci-dessous).

## Base de données `ecommerce_db`

| Fichier SQL | Rôle |
|-------------|------|
| **schema_ecommerce_db.sql** | Création complète de la base et de toutes les tables (à exécuter en premier si la base n'existe pas). |
| **migration_ajout_manquants.sql** | Ajoute tables ou colonnes manquantes (carts, order_items, colonnes products, created_at). À exécuter si la base existe déjà mais est incomplète. |
| **migration_add_provider_email.sql** | Ajout du champ email pour les fournisseurs. |
| **fix_orders_table.sql** | Corrections éventuelles sur la table des commandes. |
| **requetes_panier_categorie.sql** | Requêtes métier (panier avec catégorie/sous-catégorie, totaux) — consultation ou rapports. |
| **donnees_exemple_panier.sql** | Données d'exemple pour le panier. |

**Ordre recommandé :**
1. **Nouvelle installation** : exécuter `schema_ecommerce_db.sql` (crée la base et les tables).
2. **Base déjà partiellement créée** : exécuter `migration_ajout_manquants.sql`, puis `migration_add_provider_email.sql` et `fix_orders_table.sql` si besoin (ignorer les erreurs « Duplicate column » ou « Table already exists » si déjà appliqué).

Exemple en ligne de commande MySQL :

```bash
mysql -u root -p < schema_ecommerce_db.sql
```

## Configuration base de données

Dans `src/main/java/Utlis/DataSource.java`, vérifier l'URL, l'utilisateur et le mot de passe MySQL :

```java
private final String url = "jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private final String username = "root";
private final String password = "";
```

## Lancer l'application

### Avec Maven (ligne de commande)

```bash
cd "C:\Users\pc\Desktop\Marketly E-commerce"
mvn clean compile javafx:run
```

Ou pour créer un JAR :

```bash
mvn clean package
# Puis lancer avec Java en ajoutant le module path JavaFX si nécessaire.
```

### Avec IntelliJ IDEA

1. **Ouvrir le projet** : File → Open → sélectionner le dossier **Marketly E-commerce** (Maven importe le `pom.xml`).
2. **Configurer le JDK** : File → Project Structure → Project → SDK 8 (ou 11+).
3. **Lancer** :
   - Utiliser la configuration d'exécution **MainAppFXML** (Run → Edit Configurations, ajouter une Application, main class : `Application.MainAppFXML` ; si besoin, VM options : `--module-path <chemin JavaFX> --add-modules javafx.controls,javafx.fxml`).
   - Ou exécuter la classe `Application.MainAppFXML` (clic droit → Run).

Si JavaFX n'est pas reconnu par le JDK, les dépendances Maven (javafx-controls, javafx-fxml, etc.) et le plugin `javafx-maven-plugin` gèrent l'exécution avec `mvn javafx:run`.

## Structure du projet

```
Marketly E-commerce/
├── pom.xml
├── README.md
├── schema_ecommerce_db.sql
├── migration_ajout_manquants.sql
├── migration_add_provider_email.sql
├── fix_orders_table.sql
├── requetes_panier_categorie.sql
├── donnees_exemple_panier.sql
├── src/main/java/
│   ├── Application/     # Contrôleurs JavaFX, MainAppFXML, SessionManager, ThemeManager, LanguageManager, Login, Register, etc.
│   ├── Entite/          # Category, Customer, Order, OrderItem, Panier, Product, Provider, SubCategory, User, stats
│   ├── Services/        # Services métier (CRUD, Order, Panier, Report, Email)
│   └── Utlis/           # DataSource
├── src/main/resources/
│   ├── *.fxml           # Vues (login, register, main, dashboard, client, panier_client, category, product, order, statistics, etc.)
│   ├── styles.css
│   ├── mail.properties
│   └── messages_fr.properties, messages_en.properties, messages_ar.properties
└── .idea/               # Config IntelliJ
```

## Technologies

- **Java 8**, **Maven**
- **JavaFX 17** (OpenJFX) : interface graphique
- **MySQL** : base de données
- **OpenPDF** : génération de factures PDF
- **JavaMail** : envoi d’emails (config dans `mail.properties`)
