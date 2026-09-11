package com.medibook.service;

import com.medibook.metier.CalculateurCreneaux;
import com.medibook.metier.PlageHoraire;
import com.medibook.model.Consultation;
import com.medibook.model.Disponibilite;
import com.medibook.model.Medecin;
import com.medibook.model.Patient;
import com.medibook.model.RendezVous;
import com.medibook.model.StatutRendezVous;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Cœur de l'application : réservation et suivi des rendez-vous.
 * <p>
 * Toutes les règles importantes sont vérifiées <b>ici, côté serveur</b>,
 * même si l'interface les vérifie déjà : on ne fait jamais confiance
 * aux données envoyées par le navigateur.
 */
@Stateless
public class RendezVousService {

    @PersistenceContext(unitName = "medibookPU")
    private EntityManager em;

    // =====================================================================
    // Créneaux
    // =====================================================================

    /**
     * Calcule les créneaux encore libres d'un médecin pour une date donnée.
     * <ol>
     *   <li>on récupère ses disponibilités pour ce jour de la semaine ;</li>
     *   <li>on récupère les heures déjà réservées ce jour-là ;</li>
     *   <li>on délègue le calcul à {@link CalculateurCreneaux} (logique pure, testée unitairement).</li>
     * </ol>
     */
    public List<LocalTime> creneauxDisponibles(Long medecinId, LocalDate date) {
        if (medecinId == null || date == null) {
            return List.of();
        }
        if (em.find(Medecin.class, medecinId) == null) {
            throw new MetierException("Médecin introuvable.");
        }

        List<PlageHoraire> plages = em.createNamedQuery("Disponibilite.parMedecinEtJour", Disponibilite.class)
                .setParameter("medecinId", medecinId)
                .setParameter("jour", date.getDayOfWeek())
                .getResultList()
                .stream()
                .map(d -> new PlageHoraire(d.getHeureDebut(), d.getHeureFin()))
                .toList();

        List<LocalDateTime> occupees = em.createNamedQuery("RendezVous.heuresOccupees", LocalDateTime.class)
                .setParameter("medecinId", medecinId)
                .setParameter("statuts", StatutRendezVous.OCCUPANT_UN_CRENEAU)
                .setParameter("debut", date.atStartOfDay())
                .setParameter("fin", date.plusDays(1).atStartOfDay())
                .getResultList();

        return CalculateurCreneaux.creneauxLibres(plages, date, occupees, LocalDateTime.now());
    }

    // =====================================================================
    // Actions du patient
    // =====================================================================

    /**
     * Réserve un rendez-vous (statut initial : EN_ATTENTE de confirmation par le médecin).
     *
     * @throws MetierException si le créneau n'est plus libre, est passé, ou si le patient
     *                         a déjà un rendez-vous à la même heure
     */
    public RendezVous reserver(Long patientId, Long medecinId, LocalDateTime dateHeure, String motif) {
        Patient patient = em.find(Patient.class, patientId);
        Medecin medecin = em.find(Medecin.class, medecinId);
        if (patient == null || medecin == null) {
            throw new MetierException("Patient ou médecin introuvable.");
        }
        if (dateHeure == null || !dateHeure.isAfter(LocalDateTime.now())) {
            throw new MetierException("Impossible de réserver un créneau passé.");
        }

        // Le créneau doit faire partie des créneaux libres (disponibilité + pas déjà pris)
        List<LocalTime> libres = creneauxDisponibles(medecinId, dateHeure.toLocalDate());
        if (!libres.contains(dateHeure.toLocalTime())) {
            throw new MetierException("Ce créneau n'est plus disponible. Veuillez en choisir un autre.");
        }

        // Un patient ne peut pas être à deux endroits en même temps
        long dejaPris = em.createNamedQuery("RendezVous.compterPatientALHeure", Long.class)
                .setParameter("patientId", patientId)
                .setParameter("statuts", StatutRendezVous.OCCUPANT_UN_CRENEAU)
                .setParameter("dateHeure", dateHeure)
                .getSingleResult();
        if (dejaPris > 0) {
            throw new MetierException("Vous avez déjà un rendez-vous à cette heure-là.");
        }

        RendezVous rdv = new RendezVous(patient, medecin, dateHeure, motif);
        em.persist(rdv);
        return rdv;
    }

    /**
     * Annulation par le patient (uniquement ses propres rendez-vous, non passés).
     */
    public void annuler(Long rendezVousId, Long patientId) {
        RendezVous rdv = trouverObligatoire(rendezVousId);
        if (!rdv.getPatient().getId().equals(patientId)) {
            throw new MetierException("Ce rendez-vous ne vous appartient pas.");
        }
        if (!rdv.getStatut().peutEtreAnnule()) {
            throw new MetierException("Un rendez-vous « " + rdv.getStatut().getLibelle() + " » ne peut pas être annulé.");
        }
        if (rdv.isPasse()) {
            throw new MetierException("Impossible d'annuler un rendez-vous passé.");
        }
        rdv.setStatut(StatutRendezVous.ANNULE);
    }

    public List<RendezVous> rendezVousDuPatient(Long patientId) {
        return em.createNamedQuery("RendezVous.parPatient", RendezVous.class)
                .setParameter("patientId", patientId)
                .getResultList();
    }

    // =====================================================================
    // Actions du médecin
    // =====================================================================

    public List<RendezVous> rendezVousDuMedecin(Long medecinId) {
        return em.createNamedQuery("RendezVous.parMedecin", RendezVous.class)
                .setParameter("medecinId", medecinId)
                .getResultList();
    }

    public void confirmer(Long rendezVousId, Long medecinId) {
        RendezVous rdv = rendezVousDuMedecinObligatoire(rendezVousId, medecinId);
        if (!rdv.getStatut().peutEtreConfirmeOuRefuse()) {
            throw new MetierException("Seul un rendez-vous en attente peut être confirmé.");
        }
        rdv.setStatut(StatutRendezVous.CONFIRME);
    }

    public void refuser(Long rendezVousId, Long medecinId) {
        RendezVous rdv = rendezVousDuMedecinObligatoire(rendezVousId, medecinId);
        if (!rdv.getStatut().peutEtreConfirmeOuRefuse()) {
            throw new MetierException("Seul un rendez-vous en attente peut être refusé.");
        }
        rdv.setStatut(StatutRendezVous.REFUSE);
    }

    /**
     * Clôture un rendez-vous confirmé en enregistrant le compte rendu de consultation.
     */
    public void terminer(Long rendezVousId, Long medecinId, String notes, String ordonnance) {
        RendezVous rdv = rendezVousDuMedecinObligatoire(rendezVousId, medecinId);
        if (!rdv.getStatut().peutEtreTermine()) {
            throw new MetierException("Seul un rendez-vous confirmé peut être terminé.");
        }
        if (notes == null || notes.isBlank()) {
            throw new MetierException("Les notes de consultation sont obligatoires.");
        }
        rdv.setConsultation(new Consultation(notes, ordonnance)); // enregistrée grâce à cascade = ALL
        rdv.setStatut(StatutRendezVous.TERMINE);
    }

    // =====================================================================
    // Méthodes utilitaires privées
    // =====================================================================

    private RendezVous trouverObligatoire(Long id) {
        RendezVous rdv = id == null ? null : em.find(RendezVous.class, id);
        if (rdv == null) {
            throw new MetierException("Rendez-vous introuvable.");
        }
        return rdv;
    }

    private RendezVous rendezVousDuMedecinObligatoire(Long rendezVousId, Long medecinId) {
        RendezVous rdv = trouverObligatoire(rendezVousId);
        if (!rdv.getMedecin().getId().equals(medecinId)) {
            throw new MetierException("Ce rendez-vous ne vous concerne pas.");
        }
        return rdv;
    }
}
