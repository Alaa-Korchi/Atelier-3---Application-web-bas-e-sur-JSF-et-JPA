package ma.fstt.ecommerce.dao;

import ma.fstt.ecommerce.model.Produit;
import ma.fstt.ecommerce.model.Vitrine;

import javax.persistence.TypedQuery;
import java.util.List;

public class VitrineDAO extends AbstractDAO<Vitrine> {

    private static final long serialVersionUID = 1L;

    public VitrineDAO() {
        super(Vitrine.class);
    }

    /** La première vitrine créée est la vitrine principale du site. */
    public Vitrine trouverPrincipale() {
        return lecture(em -> {
            List<Vitrine> r = em.createQuery("SELECT v FROM Vitrine v ORDER BY v.id", Vitrine.class)
                    .setMaxResults(1)
                    .getResultList();
            return r.isEmpty() ? null : r.get(0);
        });
    }

    /** Produits de la vitrine, filtrés (facultativement) par catégorie et par mot-clé dans le nom. */
    public List<Produit> produitsDeLaVitrine(Long vitrineId, Long categorieId, String motCle) {
        boolean avecMotCle = motCle != null && !motCle.trim().isEmpty();
        return lecture(em -> {
            StringBuilder jpql = new StringBuilder("SELECT p FROM Vitrine v JOIN v.produits p WHERE v.id = :vid");
            if (categorieId != null) {
                jpql.append(" AND p.categorie.id = :cid");
            }
            if (avecMotCle) {
                jpql.append(" AND LOWER(p.nom) LIKE :kw");
            }
            jpql.append(" ORDER BY p.nom");

            TypedQuery<Produit> q = em.createQuery(jpql.toString(), Produit.class).setParameter("vid", vitrineId);
            if (categorieId != null) {
                q.setParameter("cid", categorieId);
            }
            if (avecMotCle) {
                q.setParameter("kw", "%" + motCle.trim().toLowerCase() + "%");
            }
            return q.getResultList();
        });
    }
}
