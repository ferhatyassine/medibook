package com.medibook.web;

import com.medibook.model.Utilisateur;
import com.medibook.securite.SessionUtilisateur;
import com.medibook.service.UtilisateurService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

/**
 * Bean de la page de connexion (login.xhtml) et de la déconnexion.
 * <p>
 * <b>@RequestScoped</b> : une nouvelle instance à chaque requête HTTP.
 * C'est suffisant ici car on n'a pas besoin de garder l'email ou le mot de passe
 * d'une requête à l'autre (et c'est plus sûr).
 */
@Named
@RequestScoped
public class LoginBean {

    @Inject
    private UtilisateurService utilisateurService;

    @Inject
    private SessionUtilisateur session;

    // Champs liés au formulaire via #{loginBean.email} et #{loginBean.motDePasse}
    @NotBlank(message = "Veuillez saisir votre email")
    private String email;

    @NotBlank(message = "Veuillez saisir votre mot de passe")
    private String motDePasse;

    /**
     * Action du bouton « Se connecter ».
     *
     * @return la page vers laquelle naviguer, ou null pour rester sur la page (en cas d'erreur)
     */
    public String connecter() {
        Optional<Utilisateur> utilisateur = utilisateurService.authentifier(email, motDePasse);
        if (utilisateur.isEmpty()) {
            // Message volontairement vague : on ne dit pas si c'est l'email ou le mot de passe qui est faux
            JsfUtil.erreur("Email ou mot de passe incorrect.");
            return null;
        }

        // Protection contre la « fixation de session » : on change l'identifiant de session à la connexion
        HttpServletRequest requete = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        requete.changeSessionId();

        session.connecter(utilisateur.get());
        JsfUtil.info("Bienvenue " + session.getNomComplet() + " !");
        JsfUtil.conserverMessagesApresRedirection();
        return session.pageAccueil();
    }

    /**
     * Déconnexion : on vide le bean de session puis on invalide la session HTTP.
     */
    public String deconnecter() {
        session.deconnecter();
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession();
        return "/index?faces-redirect=true";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
}
