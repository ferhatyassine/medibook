package com.medibook.web;

import com.medibook.model.Disponibilite;
import com.medibook.securite.SessionUtilisateur;
import com.medibook.service.MedecinService;
import com.medibook.service.MetierException;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bean de la page « Mes disponibilités » du médecin (medecin/disponibilites.xhtml).
 */
@Named
@ViewScoped
public class DisponibiliteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MedecinService medecinService;
    @Inject
    private SessionUtilisateur session;

    private List<Disponibilite> disponibilites;

    // --- Formulaire d'ajout ---
    private DayOfWeek jour = DayOfWeek.MONDAY;
    private String debut = "09:00";
    private String fin = "12:00";

    @PostConstruct
    public void init() {
        charger();
    }

    private void charger() {
        disponibilites = medecinService.disponibilites(session.getId());
    }

    public void ajouter() {
        try {
            medecinService.ajouterDisponibilite(session.getId(), jour, LocalTime.parse(debut), LocalTime.parse(fin));
            JsfUtil.info("Disponibilité ajoutée.");
        } catch (MetierException e) {
            JsfUtil.erreur(e.getMessage());
        }
        charger();
    }

    public void supprimer(Long id) {
        try {
            medecinService.supprimerDisponibilite(id, session.getId());
            JsfUtil.info("Disponibilité supprimée.");
        } catch (MetierException e) {
            JsfUtil.erreur(e.getMessage());
        }
        charger();
    }

    /** Jours proposés dans la liste déroulante. */
    public DayOfWeek[] getJours() {
        return DayOfWeek.values();
    }

    /** Heures proposées : de 07:00 à 20:00 par pas de 30 minutes. */
    public List<String> getHeures() {
        List<String> heures = new ArrayList<>();
        for (LocalTime h = LocalTime.of(7, 0); !h.isAfter(LocalTime.of(20, 0)); h = h.plusMinutes(30)) {
            heures.add(h.toString()); // LocalTime.toString() donne "07:00", "07:30"...
        }
        return heures;
    }

    /** Traduit un jour en français : MONDAY → "Lundi". Appelée depuis la page : #{disponibiliteBean.libelleJour(j)} */
    public String libelleJour(DayOfWeek j) {
        String nom = j.getDisplayName(TextStyle.FULL, Locale.FRENCH);
        return nom.substring(0, 1).toUpperCase() + nom.substring(1);
    }

    public List<Disponibilite> getDisponibilites() {
        return disponibilites;
    }

    public DayOfWeek getJour() {
        return jour;
    }

    public void setJour(DayOfWeek jour) {
        this.jour = jour;
    }

    public String getDebut() {
        return debut;
    }

    public void setDebut(String debut) {
        this.debut = debut;
    }

    public String getFin() {
        return fin;
    }

    public void setFin(String fin) {
        this.fin = fin;
    }
}
