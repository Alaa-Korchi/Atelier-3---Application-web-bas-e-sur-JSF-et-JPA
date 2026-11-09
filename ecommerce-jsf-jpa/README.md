# Atelier JSF + JPA — Site e-commerce (MarocShop)

Application web Java EE : **JSF 2.2** (Mojarra) + **JPA** (EclipseLink) + **MySQL**, déployée sur **Tomcat 9**.
Gestion de la **vitrine**, des **internautes** (inscription / connexion) et du **panier**.

## Prérequis

- JDK 11 ou 17 (le projet compile en Java 11)
- MySQL 8 démarré (utilisateur `root`, mot de passe vide par défaut — voir `persistence.xml`)
- Tomcat **9.x** (⚠ pas Tomcat 10 : il utilise `jakarta.*` au lieu de `javax.*`)
- IntelliJ IDEA (Community ou Ultimate)

## Ouvrir le projet dans IntelliJ

1. `File > Open…` et choisir le fichier **`pom.xml`** → *Open as Project*. IntelliJ importe les dépendances Maven.
2. Vérifier le JDK : `File > Project Structure > Project SDK` (11 ou 17).

## Configurer la base MySQL

Rien à créer à la main : l'URL contient `createDatabaseIfNotExist=true` et EclipseLink génère les tables
(`eclipselink.ddl-generation=create-or-extend-tables`). Adaptez seulement `user` / `password` dans
`src/main/resources/META-INF/persistence.xml`.
Au premier démarrage, un jeu de données de démonstration (3 catégories, 9 produits, 1 vitrine) est inséré.

## Lancer sur Tomcat depuis IntelliJ

**Ultimate** : `Run > Edit Configurations > + > Tomcat Server > Local`, onglet *Deployment* : `+ > Artifact > ecommerce:war exploded`,
*Application context* : `/ecommerce`.

**Community** : installer le plugin **Smart Tomcat** (`Settings > Plugins`), puis `Run > Edit Configurations > + > Smart Tomcat` :
Tomcat Server = dossier de Tomcat 9, Deployment Directory = `src/main/webapp`, Context Path = `/ecommerce`.

Alternative sans IDE : `mvn clean package`, puis copier `target/ecommerce.war` dans `<TOMCAT>/webapps/`.

Ouvrir ensuite : <http://localhost:8080/ecommerce/>

## Tests

`mvn test` exécute `PersistanceTest` (base H2 en mémoire, aucun MySQL requis) : mapping JPA, requêtes, cycle de vie du panier.

## Structure

```
src/main/java/ma/fstt/ecommerce/
  model/     Categorie, Produit, Vitrine, Internaute, Panier, LignePanier   (entités JPA)
  dao/       AbstractDAO + un DAO par entité (transactions locales)
  util/      JPAUtil (EntityManagerFactory), PasswordUtil, DataInitializer
  bean/      VitrineBean, InternauteBean, PanierBean, ProduitBean           (beans JSF)
  listener/  AppListener (démarre / ferme JPA avec l'application)
src/main/resources/META-INF/persistence.xml
src/main/webapp/   template.xhtml, index, login, inscription, panier, produits (+ WEB-INF, css)
docs/              diagramme de classes (.dot et .png)
```

## Écarts volontaires par rapport au pom de l'énoncé

- `javax.persistence 2.0.0` retiré : EclipseLink 2.7 embarque déjà l'API JPA 2.2, les deux versions entreraient en conflit.
- `javax.servlet-api` en scope `provided` : Tomcat la fournit, elle ne doit pas être dans le WAR.
- Les balises du pom de l'énoncé étaient inversées (`>dependency<`) : elles ont été remises en forme.
