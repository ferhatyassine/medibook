package com.medibook.web;

import com.medibook.model.RendezVous;
import com.medibook.model.StatutRendezVous;
import com.medibook.securite.SessionUtilisateur;
import com.medibook.service.MetierException;
import com.medibook.service.RendezVousService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/**
 * Bean du planning du médecin (medecin/planning.xhtml) :
 * liste des rendez-vous, confirmation / refus, saisie de la consultation.
 */
@Named
@ViewScoped
public class PlanningMedecinBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private RendezVousService rendezVousService;
    @Inject
    private SessionUtilisateur session;

    private List<RendezVous> rendezVous;

    /** Filtre d'affichage : true = masquer les rendez-vous annulés/refusés. */
    private boolean masquerInactifs = true;

    // --- Boîte de dialogue « Consultation » ---
    private RendezVous selection;
    private String notes;
    private String ordonnance;

    @PostConstruct
    public void init() {
        charger();
    }

    /** Recharge la liste depuis la base, en appliquant le filtre. */
    public void charger() {
        List<RendezVous> tous = rendezVousService.rendezVousDuMedecin(session.getId());
        rendezVous = masquerInactifs
                ? tous.stream()
                      .filter(r -> r.getStatut() != StatutRendezVous.ANNULE && r.getStatut() != StatutRendezVous.REFUSE)
                      .toList()
                : tous;
    }

    public void confirmer(Long id) {
        executer(() -> rendezVousService.confirmer(id, session.getId()), "Rendez-vous confirmé.");
    }

    public void refuser(Long id) {
        executer(() -> rendezVousService.refuser(id, session.getId()), "Rendez-vous refusé.");
    }

    /** Prépare la boîte de dialogue pour le rendez-vous choisi. */
    public void preparerConsultation(RendezVous rdv) {
        this.selection = rdv;
        this.notes = null;
        this.ordonnance = null;
    }

    /** Enregistre la consultation et clôture le rendez-vous. */
    public void terminer() {
        if (selection == null) {
            return;
        }
        executer(() -> rendezVousService.terminer(selection.getId(), session.getId(), notes, ordonnance),
                "Consultation enregistrée.");
    }

    /**
     * Exécute une action métier, affiche le message de succès ou d'erreur,
     * puis recharge la liste. {@code Runnable} permet de passer « un bout de code » en paramètre.
     */
    private void executer(Runnable action, String messageSucces) {
        try {
            action.run();
            JsfUtil.info(messageSucces);
        } catch (MetierException e) {
            JsfUtil.erreur(e.getMessage());
        }
        charger();
    }

    /** Nombre de demandes en attente (affiché en haut de page). */
    public long getNombreEnAttente() {
        return rendezVous.stream().filter(r -> r.getStatut() == StatutRendezVous.EN_ATTENTE).count();
    }

    public List<RendezVous> getRendezVous() {
        return rendezVous;
    }

    public boolean isMasquerInactifs() {
        return masquerInactifs;
    }

    public void setMasquerInactifs(boolean masquerInactifs) {
        this.masquerInactifs = masquerInactifs;
    }

    public RendezVous getSelection() {
        return selection;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getOrdonnance() {
        return ordonnance;
    }

    public void setOrdonnance(String ordonnance) {
        this.ordonnance = ordonnance;
    }
}
