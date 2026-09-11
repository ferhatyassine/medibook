package com.medibook.web;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 * Petites méthodes utilitaires pour afficher des messages dans les pages JSF.
 * <p>
 * Les messages s'affichent dans le composant {@code <p:growl>} du gabarit (layout.xhtml).
 */
public final class JsfUtil {

    private JsfUtil() {
    }

    /** Message de succès (vert). */
    public static void info(String message) {
        ajouter(FacesMessage.SEVERITY_INFO, message);
    }

    /** Message d'avertissement (orange). */
    public static void avertissement(String message) {
        ajouter(FacesMessage.SEVERITY_WARN, message);
    }

    /** Message d'erreur (rouge). */
    public static void erreur(String message) {
        ajouter(FacesMessage.SEVERITY_ERROR, message);
    }

    /**
     * Conserve les messages après une redirection (faces-redirect=true).
     * Sans cela, le message serait perdu car la redirection crée une nouvelle requête.
     * Le « Flash scope » garde les données le temps d'une seule redirection.
     */
    public static void conserverMessagesApresRedirection() {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
    }

    private static void ajouter(FacesMessage.Severity severite, String message) {
        // clientId null = message global (non rattaché à un champ précis)
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severite, message, null));
    }
}
