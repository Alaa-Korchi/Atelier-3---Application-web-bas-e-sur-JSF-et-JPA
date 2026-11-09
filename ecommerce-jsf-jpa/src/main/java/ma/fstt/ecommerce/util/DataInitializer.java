package ma.fstt.ecommerce.util;

import ma.fstt.ecommerce.model.Categorie;
import ma.fstt.ecommerce.model.Produit;
import ma.fstt.ecommerce.model.Vitrine;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Insère un jeu de données de démonstration au premier démarrage (base vide). */
public final class DataInitializer {

    private DataInitializer() {
    }

    public static void insererSiVide() {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            Long nbVitrines = em.createQuery("SELECT COUNT(v) FROM Vitrine v", Long.class).getSingleResult();
            if (nbVitrines > 0) {
                return;
            }
            em.getTransaction().begin();

            Categorie info = new Categorie("Informatique", "Ordinateurs, composants et accessoires");
            Categorie tel = new Categorie("Téléphonie", "Smartphones et accessoires");
            Categorie maison = new Categorie("Maison", "Électroménager et décoration");
            em.persist(info);
            em.persist(tel);
            em.persist(maison);

            List<Produit> produits = new ArrayList<>();
            produits.add(new Produit("Laptop 15.6\" Core i5", "8 Go RAM, SSD 512 Go, écran Full HD", prix("6490.00"), 12, info));
            produits.add(new Produit("Souris sans fil", "Capteur optique 1600 DPI, USB", prix("129.00"), 80, info));
            produits.add(new Produit("Clavier mécanique", "Switches rouges, rétroéclairé", prix("449.00"), 35, info));
            produits.add(new Produit("Écran 24\" IPS", "1920x1080, 75 Hz, HDMI", prix("1390.00"), 18, info));
            produits.add(new Produit("Smartphone 128 Go", "6.5\", double SIM, 5G", prix("2990.00"), 25, tel));
            produits.add(new Produit("Écouteurs Bluetooth", "Réduction de bruit, 24 h d'autonomie", prix("349.00"), 60, tel));
            produits.add(new Produit("Chargeur rapide 30 W", "USB-C, compatible tous smartphones", prix("99.00"), 120, tel));
            produits.add(new Produit("Bouilloire électrique", "1,7 L, arrêt automatique", prix("179.00"), 40, maison));
            produits.add(new Produit("Aspirateur robot", "Navigation intelligente, 2 h d'autonomie", prix("1890.00"), 8, maison));

            Vitrine vitrine = new Vitrine("Vitrine principale");
            for (Produit p : produits) {
                em.persist(p);
            }
            vitrine.getProduits().addAll(produits);
            em.persist(vitrine);

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private static BigDecimal prix(String valeur) {
        return new BigDecimal(valeur);
    }
}
