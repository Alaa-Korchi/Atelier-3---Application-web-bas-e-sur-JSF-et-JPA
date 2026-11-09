package ma.fstt.ecommerce.dao;

import ma.fstt.ecommerce.model.Categorie;
import ma.fstt.ecommerce.model.Produit;
import ma.fstt.ecommerce.model.Vitrine;

import java.util.List;

public class ProduitDAO extends AbstractDAO<Produit> {

    private static final long serialVersionUID = 1L;

    public ProduitDAO() {
        super(Produit.class);
    }

    @Override
    public List<Produit> trouverTous() {
        return lecture(em -> em.createQuery("SELECT p FROM Produit p ORDER BY p.nom", Produit.class).getResultList());
    }

    /** Crée un produit dans une catégorie et (si vitrineId est fourni) le met en vitrine, en une seule transaction. */
    public Produit creer(Produit produit, Long categorieId, Long vitrineId) {
        return transaction(em -> {
            produit.setCategorie(em.getReference(Categorie.class, categorieId));
            em.persist(produit);
            if (vitrineId != null) {
                Vitrine vitrine = em.find(Vitrine.class, vitrineId);
                if (vitrine != null) {
                    vitrine.getProduits().add(produit);
                }
            }
            return produit;
        });
    }

    /** Retire d'abord le produit des vitrines (table de jointure), puis le supprime. */
    @Override
    public void supprimer(Long id) {
        transaction(em -> {
            Produit produit = em.find(Produit.class, id);
            if (produit == null) {
                return null;
            }
            List<Vitrine> vitrines = em.createQuery(
                            "SELECT DISTINCT v FROM Vitrine v JOIN v.produits p WHERE p.id = :id", Vitrine.class)
                    .setParameter("id", id)
                    .getResultList();
            for (Vitrine v : vitrines) {
                v.getProduits().removeIf(p -> p.getId().equals(id));
            }
            em.remove(produit);
            return null;
        });
    }
}
