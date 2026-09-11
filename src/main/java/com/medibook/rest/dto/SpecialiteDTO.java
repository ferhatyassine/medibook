package com.medibook.rest.dto;

import com.medibook.model.Specialite;

/**
 * DTO (Data Transfer Object) : objet simple envoyé en JSON par l'API.
 * <p>
 * <b>Pourquoi ne pas renvoyer directement l'entité JPA ?</b>
 * <ul>
 *   <li>on contrôle exactement ce qui est exposé (jamais le mot de passe d'un médecin !) ;</li>
 *   <li>on évite les boucles infinies des relations bidirectionnelles lors de la conversion JSON ;</li>
 *   <li>on peut faire évoluer la base sans casser les clients de l'API.</li>
 * </ul>
 */
public record SpecialiteDTO(Long id, String libelle, String description) {

    /** Méthode de fabrique : convertit une entité en DTO. */
    public static SpecialiteDTO de(Specialite s) {
        return new SpecialiteDTO(s.getId(), s.getLibelle(), s.getDescription());
    }
}
