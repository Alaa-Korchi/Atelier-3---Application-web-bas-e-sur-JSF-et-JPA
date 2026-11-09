package ma.fstt.ecommerce.model;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Panier d'un internaute : un panier par internaute, composé de lignes (produit + quantité). */
@Entity
@Table(name = "panier")
public class Panier implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "internaute_id", unique = true)
    private Internaute internaute;

    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LignePanier> lignes = new ArrayList<>();

    public Panier() {
    }

    // ---------- logique métier ----------

    /** Ajoute {@code quantite} exemplaires du produit (fusionne avec la ligne existante, vérifie le stock). */
    public void ajouterProduit(Produit produit, int quantite) {
        LignePanier ligne = trouverLigneParProduit(produit.getId());
        int nouvelleQuantite = (ligne == null ? 0 : ligne.getQuantite()) + quantite;
        if (nouvelleQuantite > produit.getStock()) {
            throw new IllegalArgumentException("Stock insuffisant pour « " + produit.getNom()
                    + " » (disponible : " + produit.getStock() + ").");
        }
        if (ligne == null) {
            lignes.add(new LignePanier(this, produit, quantite));
        } else {
            ligne.setQuantite(nouvelleQuantite);
        }
    }

    public LignePanier trouverLigneParProduit(Long produitId) {
        for (LignePanier l : lignes) {
            if (l.getProduit().getId().equals(produitId)) {
                return l;
            }
        }
        return null;
    }

    public LignePanier trouverLigne(Long ligneId) {
        for (LignePanier l : lignes) {
            if (ligneId.equals(l.getId())) {
                return l;
            }
        }
        return null;
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (LignePanier l : lignes) {
            total = total.add(l.getSousTotal());
        }
        return total;
    }

    public int getNbArticles() {
        int n = 0;
        for (LignePanier l : lignes) {
            n += l.getQuantite();
        }
        return n;
    }

    // ---------- accesseurs ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Internaute getInternaute() { return internaute; }
    public void setInternaute(Internaute internaute) { this.internaute = internaute; }
    public List<LignePanier> getLignes() { return lignes; }
    public void setLignes(List<LignePanier> lignes) { this.lignes = lignes; }

    @Override
    public String toString() {
        return "Panier[" + id + ", " + getNbArticles() + " articles]";
    }
}
