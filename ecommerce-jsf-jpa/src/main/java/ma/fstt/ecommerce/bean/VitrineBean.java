package ma.fstt.ecommerce.bean;

import ma.fstt.ecommerce.dao.CategorieDAO;
import ma.fstt.ecommerce.dao.VitrineDAO;
import ma.fstt.ecommerce.model.Categorie;
import ma.fstt.ecommerce.model.Produit;
import ma.fstt.ecommerce.model.Vitrine;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** Gestion de la vitrine : affichage des produits avec filtre par catégorie et recherche par mot-clé. */
@ManagedBean(name = "vitrineBean")
@ViewScoped
public class VitrineBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final VitrineDAO vitrineDAO = new VitrineDAO();
    private final CategorieDAO categorieDAO = new CategorieDAO();

    private Vitrine vitrine;
    private List<Categorie> categories;
    private List<Produit> produits = Collections.emptyList();

    private Long categorieId;
    private String motCle;

    @PostConstruct
    public void init() {
        vitrine = vitrineDAO.trouverPrincipale();
        categories = categorieDAO.trouverTous();
        rechercher();
    }

    public void rechercher() {
        if (vitrine == null) {
            produits = Collections.emptyList();
            return;
        }
        Long filtre = (categorieId != null && categorieId > 0) ? categorieId : null;
        produits = vitrineDAO.produitsDeLaVitrine(vitrine.getId(), filtre, motCle);
    }

    public void reinitialiser() {
        categorieId = null;
        motCle = null;
        rechercher();
    }

    public Vitrine getVitrine() { return vitrine; }
    public List<Categorie> getCategories() { return categories; }
    public List<Produit> getProduits() { return produits; }
    public Long getCategorieId() { return categorieId; }
    public void setCategorieId(Long categorieId) { this.categorieId = categorieId; }
    public String getMotCle() { return motCle; }
    public void setMotCle(String motCle) { this.motCle = motCle; }
}
