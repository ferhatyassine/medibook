package com.medibook.rest.dto;

import java.util.List;

/**
 * Créneaux libres d'un médecin pour une date.
 * Exemple JSON : {"medecinId":1,"date":"2026-09-14","creneaux":["09:00","09:30"]}
 */
public record CreneauxDTO(Long medecinId, String date, List<String> creneaux) {
}
