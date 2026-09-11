package com.medibook.web;

import com.medibook.model.Medecin;
import com.medibook.model.Specialite;
import com.medibook.securite.SessionUtilisateur;
import com.medibook.service.MedecinService;
import com.medibook.service.MetierException;
import com.medibook.service.RendezVousService;
import com.medibook.service.SpecialiteService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean de la page de réservation (patient/reserver.xhtml).
 * <p>
 * Parcours en 4 étapes : spécialité → médecin → date → créneau (+ motif).
 * <p>
 * <b>@ViewScoped</b> : l'instance vit tant que l'utilisateur reste sur la même page.
 * Indispensable ici car les requêtes AJAX successives (changement de spécialité,
 * de date...) doivent retrouver les choix précédents. Le bean doit être {@link Serializable}.
 */
@Named
@ViewScoped
public class ReservationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Format d'affichage des heures : "09:30". */
    private static final DateTimeFormatter FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    @Inject
    private SpecialiteService specialiteService;
    @Inject
    private MedecinService medecinService;
    @Inject
    private RendezVousService rendezVousService;
    @Inject
    private SessionUtilisateur session;

    // --- Données affichées ---
    private List<Specialite> specialites;
    private List<Medecin> medecins = new ArrayList<>();
    private List<String> creneaux = new ArrayList<>();

    // --- Choix de l'utilisateur ---
    private Long specialiteId;
    private Long medecinId;
    private LocalDate date;
    private String heure;
    private String motif;

    /**
     * Appelée une seule fois, après la création du bean et l'injection des dépendances
     * (contrairement au constructeur, où les champs @Inject sont encore null).
     */
    @PostConstruct
    public void init() {
        specialites = specialiteService.toutes();
        date = LocalDate.now().plusDays(1); // demain par défaut
    }

    /** Appelée en AJAX quand la spécialité change : on recharge la liste des médecins. */
    public void onSpecialiteChange() {
        medecins = specialiteId == null ? new ArrayList<>() : medecinService.parSpecialite(specialiteId);
        medecinId = null;
        reinitialiserCreneaux();
    }

    /** Appelée en AJAX quand le médecin ou la date change : on recalcule les créneaux libres. */
    public void chargerCreneaux() {
        reinitialiserCreneaux();
        if (medecinId != null && date != null) {
            creneaux = rendezVousService.creneauxDisponibles(medecinId, date).stream()
                    .map(FORMAT_HEURE::format)
                    .toList();
        }
    }

    /** Action du bouton « Réserver ». */
    public String reserver() {
        if (medecinId == null || date == null || heure == null) {
            JsfUtil.avertissement("Veuillez choisir un médecin, une date et un créneau.");
            return null;
        }
        try {
            LocalDateTime dateHeure = LocalDateTime.of(date, LocalTime.parse(heure, FORMAT_HEURE));
            rendezVousService.reserver(session.getId(), medecinId, dateHeure, motif);
            JsfUtil.info("Demande de rendez-vous envoyée. Le médecin doit maintenant la confirmer.");
            JsfUtil.conserverMessagesApresRedirection();
            return "/patient/mes-rendez-vous?faces-redirect=true";
        } catch (MetierException e) {
            JsfUtil.erreur(e.getMessage());
            chargerCreneaux(); // le créneau a peut-être été pris entre-temps : on rafraîchit
            return null;
        }
    }

    private void reinitialiserCreneaux() {
        creneaux = new ArrayList<>();
        heure = null;
    }

    /** Médecin actuellement sélectionné (pour afficher son nom dans le récapitulatif). */
    public Medecin getMedecinChoisi() {
        return medecins.stream().filter(m -> m.getId().equals(medecinId)).findFirst().orElse(null);
    }

    /** Date minimale du calendrier : aujourd'hui. */
    public LocalDate getAujourdhui() {
        return LocalDate.now();
    }

    // ------------------------------------------------------------------ getters / setters

    public List<Specialite> getSpecialites() {
        return specialites;
    }

    public List<Medecin> getMedecins() {
        return medecins;
    }

    public List<String> getCreneaux() {
        return creneaux;
    }

    public Long getSpecialiteId() {
        return specialiteId;
    }

    public void setSpecialiteId(Long specialiteId) {
        this.specialiteId = specialiteId;
    }

    public Long getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Long medecinId) {
        this.medecinId = medecinId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}
