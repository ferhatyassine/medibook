package com.medibook.web;

import com.medibook.model.RendezVous;
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
 * Bean de la page « Mes rendez-vous » du patient (patient/mes-rendez-vous.xhtml).
 */
@Named
@ViewScoped
public class MesRendezVousBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private RendezVousService rendezVousService;
    @Inject
    private SessionUtilisateur session;

    private List<RendezVous> rendezVous;

    /** Rendez-vous dont on affiche le compte rendu dans la boîte de dialogue. */
    private RendezVous selection;

    @PostConstruct
    public void init() {
        charger();
    }

    private void charger() {
        rendezVous = rendezVousService.rendezVousDuPatient(session.getId());
    }

    /** Action du bouton « Annuler » d'une ligne du tableau. */
    public void annuler(Long id) {
        try {
            rendezVousService.annuler(id, session.getId());
            JsfUtil.info("Rendez-vous annulé.");
        } catch (MetierException e) {
            JsfUtil.erreur(e.getMessage());
        }
        charger(); // on recharge la liste pour afficher le nouveau statut
    }

    public List<RendezVous> getRendezVous() {
        return rendezVous;
    }

    public RendezVous getSelection() {
        return selection;
    }

    public void setSelection(RendezVous selection) {
        this.selection = selection;
    }
}
