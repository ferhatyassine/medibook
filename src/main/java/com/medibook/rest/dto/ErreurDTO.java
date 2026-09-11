package com.medibook.rest.dto;

/**
 * Corps JSON renvoyé en cas d'erreur. Exemple : {"erreur":"Médecin introuvable."}
 */
public record ErreurDTO(String erreur) {
}
