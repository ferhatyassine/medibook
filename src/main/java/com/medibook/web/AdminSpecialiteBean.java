package com.medibook.web;

import com.medibook.model.Specialite;
import com.medibook.service.MetierException;
import com.medibook.service.SpecialiteService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/**
 * Bean de gestion des spécialités (admin/specialites.xhtml).
 * <p>
 * Le même formulaire sert à la création et à la modification :
 * si {@code idEdition} est null on crée, sinon on modifie.
 */
@Named
@ViewScoped
public class AdminSpecialiteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SpecialiteService specialiteService;

    private List<Specialite> specialites;

    // --- Formulaire ---
    private Long idEdition;
    private String libelle;
    private String description;

    @PostConstruct
    public void init() {
        charger();
    }

    private void charger() {
        specialites = specialiteService.toutes();
    }

    /** Remplit le formulaire avec la spécialité à modifier. */
    public void editer(Specialite s) {
        idEdition = s.getId();
        libelle = s.getLibelle();
        description = s.getDescription();
    }

    /** Vide le formulaire (retour en mode création). */
    public void nouveau() {
        idEdition = null;
        libelle = null;
        description = null;
    }

    public void enregistrer() {
        try {
            if (idEdition == null) {
                specialiteService.creer(libelle, description);
                JsfUtil.info("Spécialité créée.");
            } else {
                specialiteService.modifier(idEdition, libelle, description);
                JsfUtil.info("Spécialité modifiée.");
            }
            nouveau();
            charger();
        } catch (MetierException e) {
            JsfUtil.erreur(e.getMessage());
        }
    }

    public void supprimer(Long id) {
        try {
            specialiteService.supprimer(id);
            JsfUtil.info("Spécialité supprimée.");
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

    public List<Specialite> getSpecialites() {
        return specialites;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
