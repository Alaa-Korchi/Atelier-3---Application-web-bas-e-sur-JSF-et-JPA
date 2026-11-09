package ma.fstt.ecommerce.bean;

import ma.fstt.ecommerce.dao.InternauteDAO;
import ma.fstt.ecommerce.model.Internaute;
import ma.fstt.ecommerce.util.PasswordUtil;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

/** Gestion de l'internaute : inscription, connexion, déconnexion (portée session). */
@ManagedBean(name = "internauteBean")
@SessionScoped
public class InternauteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final InternauteDAO dao = new InternauteDAO();

    /** Internaute connecté (null si visiteur anonyme). */
    private Internaute internaute;

    // champs des formulaires
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String confirmation;

    public String inscrire() {
        String emailNormalise = email.trim().toLowerCase();
        if (!motDePasse.equals(confirmation)) {
            Messages.erreur("Les deux mots de passe ne correspondent pas.");
            return null;
        }
        if (dao.existeEmail(emailNormalise)) {
            Messages.erreur("Cet email est déjà utilisé.");
            return null;
        }
        try {
            internaute = dao.enregistrer(new Internaute(nom.trim(), prenom.trim(), emailNormalise,
                    PasswordUtil.hasher(motDePasse)));
        } catch (RuntimeException e) {
            Messages.erreur("Inscription impossible : " + e.getMessage());
            return null;
        }
        viderChamps();
        return "index?faces-redirect=true";
    }

    public String connecter() {
        Internaute trouve = dao.trouverParEmail(email.trim().toLowerCase());
        if (trouve == null || !PasswordUtil.verifier(motDePasse, trouve.getMotDePasse())) {
            motDePasse = null;
            Messages.erreur("Email ou mot de passe incorrect.");
            return null;
        }
        internaute = trouve;
        viderChamps();
        return "index?faces-redirect=true";
    }

    public String deconnecter() {
        // la session entière (et donc le panier chargé en mémoire) est détruite
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "index?faces-redirect=true";
    }

    public boolean isConnecte() {
        return internaute != null;
    }

    private void viderChamps() {
        nom = null;
        prenom = null;
        email = null;
        motDePasse = null;
        confirmation = null;
    }

    public Internaute getInternaute() { return internaute; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public String getConfirmation() { return confirmation; }
    public void setConfirmation(String confirmation) { this.confirmation = confirmation; }
}
