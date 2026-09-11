package com.medibook.service;

import com.medibook.metier.CalculateurCreneaux;
import com.medibook.metier.PlageHoraire;
import com.medibook.model.Disponibilite;
import com.medibook.model.Medecin;
import com.medibook.model.Specialite;
import com.medibook.securite.MotDePasseUtil;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

/**
 * Gestion des médecins (par l'administrateur) et de leurs disponibilités (par le médecin).
 */
@Stateless
public class MedecinService {

    @PersistenceContext(unitName = "medibookPU")
    private EntityManager em;

    /** Injection d'un autre EJB : CDI fournit automatiquement l'instance. */
    @Inject
    private UtilisateurService utilisateurService;

    // =====================================================================
    // Lecture
    // =====================================================================

    public List<Medecin> tous() {
        return em.createNamedQuery("Medecin.tous", Medecin.class).getResultList();
    }

    public List<Medecin> parSpecialite(Long specialiteId) {
        return em.createNamedQuery("Medecin.parSpecialite", Medecin.class)
                .setParameter("specialiteId", specialiteId)
                .getResultList();
    }

    /** Renvoie le médecin, ou null s'il n'existe pas. */
    public Medecin trouver(Long id) {
        return id == null ? null : em.find(Medecin.class, id);
    }

    public long compter() {
        return em.createNamedQuery("Medecin.compter", Long.class).getSingleResult();
    }

    // =====================================================================
    // Création / modification / suppression (administrateur)
    // =====================================================================

    /**
     * Crée un compte médecin.
     *
     * @param medecin      nom, prénom, email, téléphone
     * @param specialiteId identifiant de la spécialité
     * @param motDePasse   mot de passe initial (en clair, il sera haché)
     */
    public Medecin creer(Medecin medecin, Long specialiteId, String motDePasse) {
        UtilisateurService.verifierMotDePasse(motDePasse);
        if (utilisateurService.emailExiste(medecin.getEmail())) {
            throw new MetierException("Un compte existe déjà avec cet email.");
        }
        medecin.setSpecialite(specialiteObligatoire(specialiteId));
        medecin.setEmail(medecin.getEmail().trim().toLowerCase());
        medecin.setMotDePasse(MotDePasseUtil.hacher(motDePasse));
        em.persist(medecin);
        return medecin;
    }

    /**
     * Met à jour les informations d'un médecin.
     * Le mot de passe n'est changé que si un nouveau est fourni.
     */
    public Medecin modifier(Long id, String nom, String prenom, String email, String telephone,
                            Long specialiteId, String nouveauMotDePasse) {
        Medecin m = em.find(Medecin.class, id);
        if (m == null) {
            throw new MetierException("Médecin introuvable.");
        }
        // L'email doit rester unique (sauf s'il s'agit de son propre email)
        utilisateurService.trouverParEmail(email)
                .filter(autre -> !autre.getId().equals(id))
                .ifPresent(autre -> {
                    throw new MetierException("Cet email est déjà utilisé par un autre compte.");
                });

        m.setNom(nom);
        m.setPrenom(prenom);
        m.setEmail(email.trim().toLowerCase());
        m.setTelephone(telephone);
        m.setSpecialite(specialiteObligatoire(specialiteId));
        if (nouveauMotDePasse != null && !nouveauMotDePasse.isBlank()) {
            UtilisateurService.verifierMotDePasse(nouveauMotDePasse);
            m.setMotDePasse(MotDePasseUtil.hacher(nouveauMotDePasse));
        }
        return m; // entité managée : les changements sont enregistrés au commit
    }

    /**
     * Supprime un médecin et ses disponibilités.
     * Refusé s'il a des rendez-vous (on ne veut pas perdre l'historique des patients).
     */
    public void supprimer(Long id) {
        Medecin m = em.find(Medecin.class, id);
        if (m == null) {
            return;
        }
        long nbRdv = em.createNamedQuery("RendezVous.compterParMedecin", Long.class)
                .setParameter("medecinId", id)
                .getSingleResult();
        if (nbRdv > 0) {
            throw new MetierException("Impossible de supprimer " + m.getNomAffiche()
                    + " : il/elle a " + nbRdv + " rendez-vous dans l'historique.");
        }
        // Requête DELETE en masse, puis suppression du médecin
        em.createNamedQuery("Disponibilite.supprimerParMedecin")
                .setParameter("medecinId", id)
                .executeUpdate();
        em.remove(m);
    }

    // =====================================================================
    // Disponibilités (médecin)
    // =====================================================================

    /**
     * Disponibilités d'un médecin, triées du lundi au dimanche puis par heure.
     * <p>
     * Le tri se fait en Java : en base, le jour est stocké en texte ("MONDAY", "FRIDAY"...)
     * et un ORDER BY le classerait par ordre alphabétique (FRIDAY avant MONDAY).
     * Les énumérations Java, elles, se comparent dans leur ordre de déclaration.
     */
    public List<Disponibilite> disponibilites(Long medecinId) {
        return em.createNamedQuery("Disponibilite.parMedecin", Disponibilite.class)
                .setParameter("medecinId", medecinId)
                .getResultList()
                .stream()
                .sorted(Comparator.comparing(Disponibilite::getJour)
                        .thenComparing(Disponibilite::getHeureDebut))
                .toList();
    }

    /**
     * Ajoute une plage de disponibilité hebdomadaire.
     *
     * @throws MetierException si les heures sont incohérentes ou chevauchent une plage existante
     */
    public Disponibilite ajouterDisponibilite(Long medecinId, DayOfWeek jour, LocalTime debut, LocalTime fin) {
        Medecin medecin = em.find(Medecin.class, medecinId);
        if (medecin == null) {
            throw new MetierException("Médecin introuvable.");
        }
        if (jour == null || debut == null || fin == null) {
            throw new MetierException("Le jour, l'heure de début et l'heure de fin sont obligatoires.");
        }
        if (!debut.isBefore(fin)) {
            throw new MetierException("L'heure de début doit être avant l'heure de fin.");
        }
        PlageHoraire nouvelle = new PlageHoraire(debut, fin);
        if (CalculateurCreneaux.decouper(nouvelle).isEmpty()) {
            throw new MetierException("La plage doit durer au moins "
                    + CalculateurCreneaux.DUREE_CRENEAU_MINUTES + " minutes.");
        }

        // Plages déjà définies ce jour-là, converties en PlageHoraire pour le calcul
        List<PlageHoraire> existantes = em.createNamedQuery("Disponibilite.parMedecinEtJour", Disponibilite.class)
                .setParameter("medecinId", medecinId)
                .setParameter("jour", jour)
                .getResultList()
                .stream()
                .map(d -> new PlageHoraire(d.getHeureDebut(), d.getHeureFin()))
                .toList();

        if (!CalculateurCreneaux.estCompatible(nouvelle, existantes)) {
            throw new MetierException("Cette plage chevauche une disponibilité existante.");
        }

        Disponibilite d = new Disponibilite(medecin, jour, debut, fin);
        em.persist(d);
        return d;
    }

    /**
     * Supprime une disponibilité. On vérifie qu'elle appartient bien au médecin
     * (un médecin ne doit pas pouvoir supprimer la disponibilité d'un collègue).
     */
    public void supprimerDisponibilite(Long disponibiliteId, Long medecinId) {
        Disponibilite d = em.find(Disponibilite.class, disponibiliteId);
        if (d == null) {
            return;
        }
        if (!d.getMedecin().getId().equals(medecinId)) {
            throw new MetierException("Cette disponibilité ne vous appartient pas.");
        }
        em.remove(d);
    }

    private Specialite specialiteObligatoire(Long specialiteId) {
        Specialite s = specialiteId == null ? null : em.find(Specialite.class, specialiteId);
        if (s == null) {
            throw new MetierException("Veuillez choisir une spécialité.");
        }
        return s;
    }
}
