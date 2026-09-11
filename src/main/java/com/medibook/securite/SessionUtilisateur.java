package com.medibook.securite;

import com.medibook.model.Role;
import com.medibook.model.Utilisateur;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * Informations sur l'utilisateur connecté, conservées pendant toute la session HTTP.
 * <p>
 * <b>@SessionScoped</b> (CDI) : une instance par session utilisateur (par navigateur).
 * Elle est détruite à la déconnexion ou après 30 minutes d'inactivité (voir web.xml).
 * <p>
 * <b>@Named</b> : rend le bean accessible dans les pages JSF sous le nom
 * {@code #{sessionUtilisateur}}, par exemple {@code #{sessionUtilisateur.nomComplet}}.
 * <p>
 * On ne garde que l'essentiel (id, nom, rôle) et pas l'entité complète,
 * pour éviter de manipuler un objet JPA « détaché » et potentiellement périmé.
 */
@Named
@SessionScoped
public class SessionUtilisateur implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nomComplet;
    private Role role;

    /** Enregistre l'utilisateur dans la session après une connexion réussie. */
    public void connecter(Utilisateur utilisateur) {
        this.id = utilisateur.getId();
        this.nomComplet = utilisateur.getNomComplet();
        this.role = utilisateur.getRole();
    }

    /** Oublie l'utilisateur (la session HTTP est aussi invalidée par LoginBean). */
    public void deconnecter() {
        this.id = null;
        this.nomComplet = null;
        this.role = null;
    }

    public boolean isConnecte() {
        return id != null;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isMedecin() {
        return role == Role.MEDECIN;
    }

    public boolean isPatient() {
        return role == Role.PATIENT;
    }

    /**
     * Page d'accueil selon le rôle (utilisée après la connexion).
     * Le suffixe "?faces-redirect=true" demande à JSF une redirection HTTP
     * (l'URL du navigateur est mise à jour).
     */
    public String pageAccueil() {
        if (role == null) {
            return "/index?faces-redirect=true";
        }
        return switch (role) {
            case ADMIN -> "/admin/statistiques?faces-redirect=true";
            case MEDECIN -> "/medecin/planning?faces-redirect=true";
            case PATIENT -> "/patient/mes-rendez-vous?faces-redirect=true";
        };
    }

    public Long getId() {
        return id;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public Role getRole() {
        return role;
    }
}
