package ma.fstt.ecommerce.bean;

import ma.fstt.ecommerce.dao.PanierDAO;
import ma.fstt.ecommerce.model.LignePanier;
import ma.fstt.ecommerce.model.Panier;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/** Gestion du panier de l'internaute connecté (portée session). */
@ManagedBean(name = "panierBean")
@SessionScoped
public class PanierBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final PanierDAO dao = new PanierDAO();

    @ManagedProperty("#{internauteBean}")
    private InternauteBean internauteBean;

    /** Copie du panier en base ; rechargée à la demande quand {@code charge} est false. */
    private Panier panier;
    private boolean charge;

    // ---------- actions ----------

    /** Ajoute une unité du produit ; un visiteur non connecté est envoyé vers la page de connexion. */
    public String ajouter(Long produitId) {
        if (!internauteBean.isConnecte()) {
            return "login?faces-redirect=true";
        }
        executer(() -> {
            panier = dao.ajouterProduit(idInternaute(), produitId, 1);
            Messages.info("Produit ajouté au panier.");
        });
        return null;
    }

    public void modifierQuantite(LignePanier ligne) {
        executer(() -> panier = dao.modifierQuantite(idInternaute(), ligne.getId(), ligne.getQuantite()));
    }

    public void retirer(LignePanier ligne) {
        executer(() -> panier = dao.retirerLigne(idInternaute(), ligne.getId()));
    }

    public void vider() {
        executer(() -> {
            panier = dao.vider(idInternaute());
            Messages.info("Le panier a été vidé.");
        });
    }

    // ---------- lecture ----------

    public Panier getPanier() {
        if (!internauteBean.isConnecte()) {
            return null;
        }
        if (!charge) {
            panier = dao.trouverParInternaute(idInternaute());
            charge = true;
        }
        return panier;
    }

    public List<LignePanier> getLignes() {
        Panier p = getPanier();
        return p == null ? Collections.<LignePanier>emptyList() : p.getLignes();
    }

    public int getNbArticles() {
        Panier p = getPanier();
        return p == null ? 0 : p.getNbArticles();
    }

    public BigDecimal getTotal() {
        Panier p = getPanier();
        return p == null ? BigDecimal.ZERO : p.getTotal();
    }

    // ---------- outils ----------

    private Long idInternaute() {
        return internauteBean.getInternaute().getId();
    }

    /** En cas d'erreur métier (stock...), on affiche le message et on recharge le panier depuis la base. */
    private void executer(Runnable action) {
        try {
            action.run();
            charge = true;
        } catch (IllegalArgumentException e) {
            Messages.erreur(e.getMessage());
            charge = false;
        } catch (RuntimeException e) {
            Messages.erreur("Opération impossible : " + e.getMessage());
            charge = false;
        }
    }

    public void setInternauteBean(InternauteBean internauteBean) {
        this.internauteBean = internauteBean;
    }
}
