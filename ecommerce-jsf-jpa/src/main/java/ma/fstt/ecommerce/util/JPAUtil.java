package ma.fstt.ecommerce.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Map;

/**
 * Point d'accès unique à l'EntityManagerFactory (coûteuse à créer : une seule instance pour toute l'application).
 * Sous Tomcat il n'y a pas d'injection @PersistenceContext, on gère donc les EntityManager à la main.
 */
public final class JPAUtil {

    public static final String UNITE_PERSISTANCE = "ecommercePU";

    private static EntityManagerFactory emf;

    private JPAUtil() {
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(UNITE_PERSISTANCE);
        }
        return emf;
    }

    /** Variante permettant de surcharger les propriétés de persistence.xml (utilisée par les tests). */
    public static synchronized void initialiser(Map<String, Object> proprietes) {
        fermer();
        emf = Persistence.createEntityManagerFactory(UNITE_PERSISTANCE, proprietes);
    }

    /** Un EntityManager par opération : à fermer dans un bloc finally. */
    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static synchronized void fermer() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
