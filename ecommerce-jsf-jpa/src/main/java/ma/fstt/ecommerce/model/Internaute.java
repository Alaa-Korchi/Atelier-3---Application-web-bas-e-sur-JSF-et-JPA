package ma.fstt.ecommerce.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/** L'internaute est le visiteur / client inscrit du site. */
@Entity
@Table(name = "internaute")
public class Internaute implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nom;

    @Column(nullable = false, length = 80)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Mot de passe haché (sel + SHA-256), jamais en clair. */
    @Column(name = "mot_de_passe", nullable = false, length = 120)
    private String motDePasse;

    @Column(name = "date_inscription", nullable = false)
    private LocalDate dateInscription = LocalDate.now();

    public Internaute() {
    }

    public Internaute(String nom, String prenom, String email, String motDePasseHache) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasseHache;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }

    @Override
    public String toString() {
        return "Internaute[" + id + ", " + email + "]";
    }
}
