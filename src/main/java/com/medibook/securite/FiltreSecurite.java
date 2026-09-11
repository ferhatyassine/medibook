package com.medibook.securite;

import com.medibook.model.Role;
import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filtre Servlet qui protège les espaces privés de l'application.
 * <p>
 * Un filtre s'exécute <b>avant</b> chaque requête correspondant à ses {@code urlPatterns}.
 * Ici, il vérifie que l'utilisateur est connecté <b>et</b> qu'il a le bon rôle :
 * <ul>
 *   <li>{@code /admin/*}   → réservé au rôle ADMIN</li>
 *   <li>{@code /medecin/*} → réservé au rôle MEDECIN</li>
 *   <li>{@code /patient/*} → réservé au rôle PATIENT</li>
 * </ul>
 * Sinon, l'utilisateur est redirigé vers la page de connexion.
 * <p>
 * Remarque : masquer un lien dans le menu ne suffit pas à sécuriser une page
 * (on peut toujours taper l'URL). C'est ce filtre qui fait la vraie vérification, côté serveur.
 */
@WebFilter(urlPatterns = {"/admin/*", "/medecin/*", "/patient/*"})
public class FiltreSecurite implements Filter {

    /** Bean CDI de session injecté automatiquement par le serveur. */
    @Inject
    private SessionUtilisateur session;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest requete = (HttpServletRequest) req;
        HttpServletResponse reponse = (HttpServletResponse) res;

        // Chemin demandé sans le contexte, ex. "/admin/medecins.xhtml"
        String chemin = requete.getRequestURI().substring(requete.getContextPath().length());
        Role roleRequis = roleRequisPour(chemin);

        if (!session.isConnecte()) {
            // Pas connecté : direction la page de connexion
            reponse.sendRedirect(requete.getContextPath() + "/login.xhtml");
            return;
        }
        if (roleRequis != null && roleRequis != session.getRole()) {
            // Connecté mais mauvais rôle : page « accès refusé »
            reponse.sendRedirect(requete.getContextPath() + "/acces-refuse.xhtml");
            return;
        }

        // Désactive le cache navigateur pour les pages privées :
        // après déconnexion, le bouton « Précédent » ne doit pas réafficher des données.
        reponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        reponse.setHeader("Pragma", "no-cache");
        reponse.setDateHeader("Expires", 0);

        // Tout va bien : on laisse passer la requête vers la page demandée
        chain.doFilter(req, res);
    }

    /** Détermine le rôle nécessaire en fonction du début de l'URL. */
    static Role roleRequisPour(String chemin) {
        if (chemin.startsWith("/admin/")) return Role.ADMIN;
        if (chemin.startsWith("/medecin/")) return Role.MEDECIN;
        if (chemin.startsWith("/patient/")) return Role.PATIENT;
        return null;
    }
}
