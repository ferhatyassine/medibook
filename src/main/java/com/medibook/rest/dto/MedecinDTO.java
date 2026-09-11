package com.medibook.rest.dto;

import com.medibook.model.Medecin;

/**
 * Représentation JSON publique d'un médecin (sans email ni mot de passe).
 */
public record MedecinDTO(Long id, String nom, String prenom, String specialite, Long specialiteId, String telephone) {

    public static MedecinDTO de(Medecin m) {
        return new MedecinDTO(m.getId(), m.getNom(), m.getPrenom(),
                m.getSpecialite().getLibelle(), m.getSpecialite().getId(), m.getTelephone());
    }
}
