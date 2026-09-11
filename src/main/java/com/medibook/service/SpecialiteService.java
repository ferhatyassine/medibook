package com.medibook.service;

import com.medibook.model.Specialite;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/**
 * CRUD (Create, Read, Update, Delete) des spécialités médicales.
 */
@Stateless
public class SpecialiteService {

    @PersistenceContext(unitName = "medibookPU")
    private EntityManager em;

    /** Toutes les spécialités, triées par libellé. */
    public List<Specialite> toutes() {
        return em.createNamedQuery("Specialite.toutes", Specialite.class).getResultList();
    }

    /** Recherche par identifiant (renvoie null si inexistant). */
    public Specialite trouver(Long id) {
        return id == null ? null : em.find(Specialite.class, id);
    }

    /**
     * Crée une nouvelle spécialité.
     *
     * @throws MetierException si le libellé existe déjà
     */
    public Specialite creer(String libelle, String description) {
        verifierLibelleUnique(libelle, null);
        Specialite s = new Specialite(libelle.trim(), description);
        em.persist(s);
        return s;
    }

    /**
     * Modifie le libellé et la description d'une spécialité existante.
     */
    public Specialite modifier(Long id, String libelle, String description) {
        Specialite s = em.find(Specialite.class, id);
        if (s == null) {
            throw new MetierException("Spécialité introuvable.");
        }
        verifierLibelleUnique(libelle, id);
        // L'entité est « managée » : les modifications seront enregistrées
        // automatiquement à la fin de la transaction (pas besoin d'appeler merge).
        s.setLibelle(libelle.trim());
        s.setDescription(description);
        return s;
    }

    /**
     * Supprime une spécialité, seulement si aucun médecin ne l'utilise.
     */
    public void supprimer(Long id) {
        Specialite s = em.find(Specialite.class, id);
        if (s == null) {
            return; // déjà supprimée
        }
        long nbMedecins = em.createNamedQuery("Medecin.compterParSpecialite", Long.class)
                .setParameter("specialiteId", id)
                .getSingleResult();
        if (nbMedecins > 0) {
            throw new MetierException("Impossible de supprimer « " + s.getLibelle()
                    + " » : " + nbMedecins + " médecin(s) y sont rattachés.");
        }
        em.remove(s);
    }

    /** Vérifie qu'aucune autre spécialité ne porte déjà ce libellé. */
    private void verifierLibelleUnique(String libelle, Long idActuel) {
        if (libelle == null || libelle.isBlank()) {
            throw new MetierException("Le libellé est obligatoire.");
        }
        List<Specialite> existantes = em.createNamedQuery("Specialite.parLibelle", Specialite.class)
                .setParameter("libelle", libelle.trim())
                .getResultList();
        boolean doublon = existantes.stream().anyMatch(s -> !s.getId().equals(idActuel));
        if (doublon) {
            throw new MetierException("La spécialité « " + libelle.trim() + " » existe déjà.");
        }
    }
}
