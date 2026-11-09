package ma.fstt.ecommerce.dao;

import ma.fstt.ecommerce.model.Categorie;

import java.util.List;

public class CategorieDAO extends AbstractDAO<Categorie> {

    private static final long serialVersionUID = 1L;

    public CategorieDAO() {
        super(Categorie.class);
    }

    @Override
    public List<Categorie> trouverTous() {
        return lecture(em -> em.createQuery("SELECT c FROM Categorie c ORDER BY c.nom", Categorie.class).getResultList());
    }
}
