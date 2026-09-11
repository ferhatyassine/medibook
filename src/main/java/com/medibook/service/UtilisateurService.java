package com.medibook.service;

import com.medibook.model.Patient;
import com.medibook.model.Utilisateur;
import com.medibook.securite.MotDePasseUtil;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

/**
 * Service métier lié aux comptes utilisateurs : connexion et inscription.
 * <p>
 * <b>@Stateless</b> (EJB) : le serveur gère un « pool » d'instances sans état.
 * Chaque méthode publique s'exécute automatiquement dans une <b>transaction</b> :
 * si tout se passe bien, elle est validée (commit) ; si une exception est levée,
 * elle est annulée (rollback). On n'écrit donc jamais {@code begin()} / {@code commit()}.
 */
@Stateless
public class UtilisateurService {

    /**
     * L'EntityManager est l'objet JPA qui fait le lien avec la base de données
     * (find, persist, merge, remove, requêtes...). Il est injecté par le serveur,
     * configuré par l'unité de persistance "medibookPU" (META-INF/persistence.xml).
     */
    @PersistenceContext(unitName = "medibookPU")
    private EntityManager em;

    /**
     * Vérifie l'email et le mot de passe.
     *
     * @return l'utilisateur si les identifiants sont corrects, sinon {@code Optional.empty()}
     */
    public Optional<Utilisateur> authentifier(String email, String motDePasse) {
        return trouverParEmail(email)
                .filter(u -> MotDePasseUtil.verifier(motDePasse, u.getMotDePasse()));
    }

    /** Recherche un utilisateur par email (sans tenir compte des majuscules). */
    public Optional<Utilisateur> trouverParEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        List<Utilisateur> resultats = em.createNamedQuery("Utilisateur.parEmail", Utilisateur.class)
                .setParameter("email", email.trim())
                .getResultList();
        // getResultList() plutôt que getSingleResult() : pas d'exception si aucun résultat
        return resultats.stream().findFirst();
    }

    public boolean emailExiste(String email) {
        return trouverParEmail(email).isPresent();
    }

    /**
     * Crée un compte patient.
     *
     * @param patient    le patient (nom, prénom, email, téléphone, date de naissance)
     * @param motDePasse le mot de passe en clair, qui sera haché
     * @return le patient enregistré (avec son identifiant)
     * @throws MetierException si l'email est déjà utilisé ou le mot de passe trop court
     */
    public Patient inscrirePatient(Patient patient, String motDePasse) {
        verifierMotDePasse(motDePasse);
        if (emailExiste(patient.getEmail())) {
            throw new MetierException("Un compte existe déjà avec cet email.");
        }
        patient.setEmail(patient.getEmail().trim().toLowerCase());
        patient.setMotDePasse(MotDePasseUtil.hacher(motDePasse));
        em.persist(patient); // INSERT dans UTILISATEUR puis dans PATIENT
        return patient;
    }

    /**
     * Crée n'importe quel utilisateur (utilisé pour l'administrateur de démonstration).
     */
    public Utilisateur creer(Utilisateur utilisateur, String motDePasse) {
        verifierMotDePasse(motDePasse);
        if (emailExiste(utilisateur.getEmail())) {
            throw new MetierException("Un compte existe déjà avec cet email.");
        }
        utilisateur.setEmail(utilisateur.getEmail().trim().toLowerCase());
        utilisateur.setMotDePasse(MotDePasseUtil.hacher(motDePasse));
        em.persist(utilisateur);
        return utilisateur;
    }

    /** Nombre total d'utilisateurs (sert à savoir si la base est vide au démarrage). */
    public long compter() {
        return em.createNamedQuery("Utilisateur.compter", Long.class).getSingleResult();
    }

    /** Règle simple : au moins 6 caractères. */
    static void verifierMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < 6) {
            throw new MetierException("Le mot de passe doit contenir au moins 6 caractères.");
        }
    }
}
