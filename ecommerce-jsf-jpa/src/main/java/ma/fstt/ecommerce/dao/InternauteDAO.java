package ma.fstt.ecommerce.dao;

import ma.fstt.ecommerce.model.Internaute;

import java.util.List;

public class InternauteDAO extends AbstractDAO<Internaute> {

    private static final long serialVersionUID = 1L;

    public InternauteDAO() {
        super(Internaute.class);
    }

    public Internaute trouverParEmail(String email) {
        return lecture(em -> {
            List<Internaute> r = em.createQuery("SELECT i FROM Internaute i WHERE i.email = :email", Internaute.class)
                    .setParameter("email", email)
                    .getResultList();
            return r.isEmpty() ? null : r.get(0);
        });
    }

    public boolean existeEmail(String email) {
        return trouverParEmail(email) != null;
    }
}
