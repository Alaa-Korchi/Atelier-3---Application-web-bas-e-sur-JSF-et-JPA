package ma.fstt.ecommerce.dao;

import ma.fstt.ecommerce.util.JPAUtil;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * DAO générique : un EntityManager par opération, transaction locale (RESOURCE_LOCAL).
 * Les entités retournées sont détachées une fois l'EntityManager fermé.
 */
public abstract class AbstractDAO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<T> type;

    protected AbstractDAO(Class<T> type) {
        this.type = type;
    }

    /** Exécute le travail dans une transaction : commit si tout va bien, rollback sinon. */
    protected <R> R transaction(Function<EntityManager, R> travail) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            R resultat = travail.apply(em);
            em.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /** Exécute une lecture (sans transaction). */
    protected <R> R lecture(Function<EntityManager, R> travail) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return travail.apply(em);
        } finally {
            em.close();
        }
    }

    /** persist() pour une nouvelle entité (id null), merge() pour une entité existante. */
    public T enregistrer(T entite) {
        return transaction(em -> {
            Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entite);
            if (id == null) {
                em.persist(entite);
                return entite;
            }
            return em.merge(entite);
        });
    }

    public T trouver(Long id) {
        return lecture(em -> em.find(type, id));
    }

    public List<T> trouverTous() {
        return lecture(em -> em.createQuery("SELECT e FROM " + type.getSimpleName() + " e", type).getResultList());
    }

    public void supprimer(Long id) {
        transaction(em -> {
            T entite = em.find(type, id);
            if (entite != null) {
                em.remove(entite);
            }
            return null;
        });
    }
}
