package ma.fstt.ecommerce.listener;

import ma.fstt.ecommerce.util.DataInitializer;
import ma.fstt.ecommerce.util.JPAUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Démarre JPA (et crée les tables) au lancement de l'application, la ferme proprement à l'arrêt. */
@WebListener
public class AppListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            JPAUtil.getEntityManagerFactory();
            DataInitializer.insererSiVide();
            LOG.info("JPA initialisé : base de données prête.");
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Impossible d'initialiser JPA : MySQL est-il démarré et "
                    + "persistence.xml (url / utilisateur / mot de passe) est-il correct ?", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JPAUtil.fermer();
    }
}
