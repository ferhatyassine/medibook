package com.medibook.web;

import com.medibook.model.Medecin;
import com.medibook.model.Specialite;
import com.medibook.service.MedecinService;
import com.medibook.service.MetierException;
import com.medibook.service.SpecialiteService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/**
 * Bean de gestion des médecins par l'administrateur (admin/medecins.xhtml).
 */
@Named
@ViewScoped
public class AdminMedecinBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MedecinService medecinService;
    @Inject
    private SpecialiteService specialiteService;

    private List<Medecin> medecins;
    private List<Specialite> specialites;

    // --- Formulaire (création ou modification) ---
    private Long idEdition;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Long specialiteId;
    private String motDePasse;

    @PostConstruct
    public void init() {
        specialites = specialiteService.toutes();
        charger();
    }

    private void charger() {
        medecins = medecinService.tous();
    }

    public void editer(Medecin m) {
        idEdition = m.getId();
        nom = m.getNom();
        prenom = m.getPrenom();
        email = m.getEmail();
        telephone = m.getTelephone();
        specialiteId = m.getSpecialite().getId();
        motDePasse = null; // on ne réaffiche jamais un mot de passe
    }

    public void nouveau() {
        idEdition = null;
        nom = null;
        prenom = null;
        email = null;
        telephone = null;
        specialiteId = null;
        motDePasse = null;
    }

    public void enregistrer() {
        try {
            if (idEdition == null) {
                Medecin m = new Medecin(nom, prenom, email, null);
                m.setTelephone(telephone);
                medecinService.creer(m, specialiteId, motDePasse);
                JsfUtil.info("Médecin ajouté. Il peut se connecter avec son email et le mot de passe choisi.");
            } else {
                medecinService.modifier(idEdition, nom, prenom, email, telephone, specialiteId, motDePasse);
                JsfUtil.info("Médecin modifié.");
            }
            nouveau();
            charger();
        } catch (MetierException e) {
            JsfUtil.erreur(e.getMessage());
        }
    }

    public void supprimer(Long id) {
        try {
            medecinService.supprimer(id);
            JsfUtil.info("Médecin supprimé.");
            if (id.equals(idEdition)) {
                nouveau();
            }
        } catch (MetierException e) {
            JsfUtil.erreur(e.getMessage());
        }
        charger();
    }

    public boolean isModeEdition() {
        return idEdition != null;
    }

    // ------------------------------------------------------------------ getters / setters

    public List<Medecin> getMedecins() {
        return medecins;
    }

    public List<Specialite> getSpecialites() {
        return specialites;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Long getSpecialiteId() {
        return specialiteId;
    }

    public void setSpecialiteId(Long specialiteId) {
        this.specialiteId = specialiteId;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
}
