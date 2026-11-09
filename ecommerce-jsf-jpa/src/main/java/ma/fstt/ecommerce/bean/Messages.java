package ma.fstt.ecommerce.bean;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/** Raccourcis pour afficher des messages globaux dans les pages JSF. */
final class Messages {

    private Messages() {
    }

    static void info(String texte) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, texte, null));
    }

    static void erreur(String texte) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, texte, null));
    }
}
