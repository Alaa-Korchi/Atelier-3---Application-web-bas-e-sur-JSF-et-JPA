package ma.fstt.ecommerce.bean;

import ma.fstt.ecommerce.dao.CategorieDAO;
import ma.fstt.ecommerce.dao.ProduitDAO;
import ma.fstt.ecommerce.dao.VitrineDAO;
import ma.fstt.ecommerce.model.Categorie;
import ma.fstt.ecommerce.model.Produit;
import ma.fstt.ecommerce.model.Vitrine;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.List;

/** Gestion du catalogue (page d'administration) : ajout et suppression de produits. */
@ManagedBean(name = "produitBean")
@ViewScoped
public class ProduitBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ProduitDAO produitDAO = new ProduitDAO();
    private final CategorieDAO categorieDAO = new CategorieDAO();
    private final VitrineDAO vitrineDAO = new VitrineDAO();

    private Produit produit = new Produit();
    private Long categorieId;
    private List<Produit> produits;
    private List<Categorie> categories;

    @PostConstruct
    public void init() {
        categories = categorieDAO.trouverTous();
        chargerProduits();
    }

    public void enregistrer() {
        if (categorieId == null || categorieId <= 0) {
            Messages.erreur("Choisissez une catégorie.");
            return;
        }
        try {
            Vitrine vitrine = vitrineDAO.trouverPrincipale();
            produitDAO.creer(produit, categorieId, vitrine == null ? null : vitrine.getId());
            Messages.info("Produit « " + produit.getNom() + " » ajouté à la vitrine.");
            produit = new Produit();
            categorieId = null;
            chargerProduits();
        } catch (RuntimeException e) {
            Messages.erreur("Enregistrement impossible : " + e.getMessage());
        }
    }

    public void supprimer(Long id) {
        try {
            produitDAO.supprimer(id);
            Messages.info("Produit supprimé.");
            chargerProduits();
        } catch (RuntimeException e) {
            Messages.erreur("Suppression impossible : ce produit figure sans doute dans un panier.");
        }
    }

    private void chargerProduits() {
        produits = produitDAO.trouverTous();
    }

    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    public Long getCategorieId() { return categorieId; }
    public void setCategorieId(Long categorieId) { this.categorieId = categorieId; }
    public List<Produit> getProduits() { return produits; }
    public List<Categorie> getCategories() { return categories; }
}
