package ma.fstt.ecommerce.dao;

import ma.fstt.ecommerce.model.Internaute;
import ma.fstt.ecommerce.model.LignePanier;
import ma.fstt.ecommerce.model.Panier;
import ma.fstt.ecommerce.model.Produit;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.function.BiConsumer;

public class PanierDAO extends AbstractDAO<Panier> {

    private static final long serialVersionUID = 1L;

    private static final String REQUETE_PANIER =
            "SELECT DISTINCT p FROM Panier p LEFT JOIN FETCH p.lignes l LEFT JOIN FETCH l.produit "
                    + "WHERE p.internaute.id = :id";

    public PanierDAO() {
        super(Panier.class);
    }

    /** Panier de l'internaute avec ses lignes et produits déjà chargés (JOIN FETCH), ou null s'il n'existe pas. */
    public Panier trouverParInternaute(Long internauteId) {
        return lecture(em -> chercher(em, internauteId));
    }

    public Panier ajouterProduit(Long internauteId, Long produitId, int quantite) {
        return modifier(internauteId, (em, panier) -> {
            Produit produit = em.find(Produit.class, produitId);
            if (produit == null) {
                throw new IllegalArgumentException("Produit introuvable.");
            }
            panier.ajouterProduit(produit, quantite);
        });
    }

    /** Fixe la quantité d'une ligne ; une quantité &lt;= 0 retire la ligne. */
    public Panier modifierQuantite(Long internauteId, Long ligneId, int quantite) {
        return modifier(internauteId, (em, panier) -> {
            LignePanier ligne = panier.trouverLigne(ligneId);
            if (ligne == null) {
                return;
            }
            if (quantite <= 0) {
                panier.getLignes().remove(ligne);
            } else if (quantite > ligne.getProduit().getStock()) {
                throw new IllegalArgumentException("Stock insuffisant pour « " + ligne.getProduit().getNom()
                        + " » (disponible : " + ligne.getProduit().getStock() + ").");
            } else {
                ligne.setQuantite(quantite);
            }
        });
    }

    public Panier retirerLigne(Long internauteId, Long ligneId) {
        return modifier(internauteId, (em, panier) -> panier.getLignes().removeIf(l -> ligneId.equals(l.getId())));
    }

    public Panier vider(Long internauteId) {
        return modifier(internauteId, (em, panier) -> panier.getLignes().clear());
    }

    // ---------- outils internes ----------

    private Panier modifier(Long internauteId, BiConsumer<EntityManager, Panier> action) {
        return transaction(em -> {
            Panier panier = chercher(em, internauteId);
            if (panier == null) {
                panier = new Panier();
                panier.setInternaute(em.getReference(Internaute.class, internauteId));
                em.persist(panier);
            }
            action.accept(em, panier);
            return panier;
        });
    }

    private Panier chercher(EntityManager em, Long internauteId) {
        List<Panier> r = em.createQuery(REQUETE_PANIER, Panier.class)
                .setParameter("id", internauteId)
                .getResultList();
        return r.isEmpty() ? null : r.get(0);
    }
}
