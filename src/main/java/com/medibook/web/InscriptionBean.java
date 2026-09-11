package com.medibook.web;

import com.medibook.model.Patient;
import com.medibook.securite.SessionUtilisateur;
import com.medibook.service.MetierException;
import com.medibook.service.UtilisateurService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;

/**
 * Bean de la page d'inscription d'un patient (inscription.xhtml).
 * <p>
 * Le formulaire est lié directement à un objet {@link Patient} :
 * {@code #{inscriptionBean.patient.nom}}. Les annotations de validation
 * de l'entité (@NotBlank, @Email...) sont donc appliquées automatiquement par JSF.
 */
@Named
@RequestScoped
public class InscriptionBean {

    @Inject
    private UtilisateurService utilisateurService;

    @Inject
    private SessionUtilisateur session;

    private final Patient patient = new Patient();
    private String motDePasse;
    private String confirmation;

    public String inscrire() {
        if (motDePasse == null || !motDePasse.equals(confirmation)) {
            JsfUtil.erreur("Les deux mots de passe ne correspondent pas.");
            return null;
        }
        try {
            Patient cree = utilisateurService.inscrirePatient(patient, motDePasse);

            // Connexion automatique après l'inscription
            ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
                    .changeSessionId();
            session.connecter(cree);

            JsfUtil.info("Compte créé avec succès. Vous pouvez réserver votre premier rendez-vous !");
            JsfUtil.conserverMessagesApresRedirection();
            return "/patient/reserver?faces-redirect=true";
        } catch (MetierException e) {
            JsfUtil.erreur(e.getMessage());
            return null;
        }
    }

    /** Date maximale du calendrier de naissance : aujourd'hui. */
    public LocalDate getAujourdhui() {
        return LocalDate.now();
    }

    public Patient getPatient() {
        return patient;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(String confirmation) {
        this.confirmation = confirmation;
    }
}
