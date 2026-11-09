package ma.fstt.ecommerce;

import ma.fstt.ecommerce.dao.*;
import ma.fstt.ecommerce.model.*;
import ma.fstt.ecommerce.util.DataInitializer;
import ma.fstt.ecommerce.util.JPAUtil;
import ma.fstt.ecommerce.util.PasswordUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** Teste la couche JPA (mapping, requêtes, transactions) sur une base H2 en mémoire : aucun MySQL requis. */
public class PersistanceTest {

    private static final VitrineDAO vitrineDAO = new VitrineDAO();
    private static final ProduitDAO produitDAO = new ProduitDAO();
    private static final CategorieDAO categorieDAO = new CategorieDAO();
    private static final InternauteDAO internauteDAO = new InternauteDAO();
    private static final PanierDAO panierDAO = new PanierDAO();

    @BeforeClass
    public static void demarrer() {
        Map<String, Object> p = new HashMap<>();
        p.put("javax.persistence.jdbc.driver", "org.h2.Driver");
        p.put("javax.persistence.jdbc.url", "jdbc:h2:mem:ecommerce;DB_CLOSE_DELAY=-1");
        p.put("javax.persistence.jdbc.user", "sa");
        p.put("javax.persistence.jdbc.password", "");
        p.put("eclipselink.ddl-generation", "drop-and-create-tables");
        p.put("eclipselink.logging.level", "WARNING");
        JPAUtil.initialiser(p);
        DataInitializer.insererSiVide();
    }

    @AfterClass
    public static void arreter() {
        JPAUtil.fermer();
    }

    @Test
    public void donneesDeDemonstrationEtVitrine() {
        Vitrine v = vitrineDAO.trouverPrincipale();
        assertNotNull(v);
        assertEquals(9, vitrineDAO.produitsDeLaVitrine(v.getId(), null, null).size());
        assertEquals(3, categorieDAO.trouverTous().size());

        Long infoId = categorieDAO.trouverTous().stream()
                .filter(c -> c.getNom().equals("Informatique")).findFirst().get().getId();
        assertEquals(4, vitrineDAO.produitsDeLaVitrine(v.getId(), infoId, null).size());
        assertEquals(1, vitrineDAO.produitsDeLaVitrine(v.getId(), null, "SOURIS").size());
        assertEquals(0, vitrineDAO.produitsDeLaVitrine(v.getId(), infoId, "bouilloire").size());
    }

    @Test
    public void inscriptionEtMotDePasseHache() {
        String hache = PasswordUtil.hasher("secret123");
        assertNotEquals("secret123", hache);
        assertTrue(PasswordUtil.verifier("secret123", hache));
        assertFalse(PasswordUtil.verifier("autre", hache));

        Internaute i = internauteDAO.enregistrer(new Internaute("Korchi", "Alaa", "alaa@test.ma", hache));
        assertNotNull(i.getId());
        assertTrue(internauteDAO.existeEmail("alaa@test.ma"));
        assertFalse(internauteDAO.existeEmail("inconnu@test.ma"));
        assertEquals("Alaa Korchi", internauteDAO.trouverParEmail("alaa@test.ma").getNomComplet());
    }

    @Test
    public void cycleDeVieDuPanier() {
        Internaute i = internauteDAO.enregistrer(new Internaute("Test", "Panier", "panier@test.ma", PasswordUtil.hasher("x")));
        assertNull(panierDAO.trouverParInternaute(i.getId()));

        Vitrine v = vitrineDAO.trouverPrincipale();
        List<Produit> produits = vitrineDAO.produitsDeLaVitrine(v.getId(), null, null);
        Produit souris = produits.stream().filter(p -> p.getNom().startsWith("Souris")).findFirst().get();
        Produit clavier = produits.stream().filter(p -> p.getNom().startsWith("Clavier")).findFirst().get();

        // ajout + fusion des lignes d'un même produit
        panierDAO.ajouterProduit(i.getId(), souris.getId(), 1);
        Panier p = panierDAO.ajouterProduit(i.getId(), souris.getId(), 1);
        p = panierDAO.ajouterProduit(i.getId(), clavier.getId(), 1);
        assertEquals(2, p.getLignes().size());
        assertEquals(3, p.getNbArticles());
        assertEquals(new BigDecimal("707.00"), p.getTotal()); // 2 x 129 + 449

        // relecture depuis la base (lignes et produits chargés par JOIN FETCH)
        Panier relu = panierDAO.trouverParInternaute(i.getId());
        assertEquals(2, relu.getLignes().size());
        assertEquals(0, new BigDecimal("707").compareTo(relu.getTotal()));

        // modification de quantité, puis dépassement de stock
        LignePanier ligneSouris = relu.trouverLigneParProduit(souris.getId());
        relu = panierDAO.modifierQuantite(i.getId(), ligneSouris.getId(), 5);
        assertEquals(6, relu.getNbArticles());
        try {
            panierDAO.modifierQuantite(i.getId(), ligneSouris.getId(), 9999);
            fail("Le dépassement de stock aurait dû être refusé");
        } catch (IllegalArgumentException attendu) {
            assertTrue(attendu.getMessage().contains("Stock insuffisant"));
        }
        assertEquals(6, panierDAO.trouverParInternaute(i.getId()).getNbArticles()); // rollback : inchangé

        // retrait d'une ligne puis vidage
        LignePanier ligneClavier = relu.trouverLigneParProduit(clavier.getId());
        assertEquals(1, panierDAO.retirerLigne(i.getId(), ligneClavier.getId()).getLignes().size());
        assertTrue(panierDAO.vider(i.getId()).getLignes().isEmpty());
        assertTrue(panierDAO.trouverParInternaute(i.getId()).getLignes().isEmpty());
    }

    @Test
    public void creationEtSuppressionDeProduit() {
        Vitrine v = vitrineDAO.trouverPrincipale();
        Long maisonId = categorieDAO.trouverTous().stream()
                .filter(c -> c.getNom().equals("Maison")).findFirst().get().getId();
        int avant = vitrineDAO.produitsDeLaVitrine(v.getId(), null, null).size();

        Produit nouveau = produitDAO.creer(new Produit("Grille-pain", "2 fentes", new BigDecimal("149.90"), 10, null),
                maisonId, v.getId());
        assertNotNull(nouveau.getId());
        assertEquals(avant + 1, vitrineDAO.produitsDeLaVitrine(v.getId(), null, null).size());

        produitDAO.supprimer(nouveau.getId());
        assertNull(produitDAO.trouver(nouveau.getId()));
        assertEquals(avant, vitrineDAO.produitsDeLaVitrine(v.getId(), null, null).size());
    }
}
