package com.medibook.service;

import java.util.Map;

/**
 * Résultat du calcul des statistiques (tableau de bord de l'administrateur).
 * <p>
 * Record immuable, utilisé à la fois par la page JSF et par l'API REST
 * (JSON-B sait convertir les records en JSON).
 *
 * @param nombreRendezVous  nombre total de rendez-vous
 * @param nombreMedecins    nombre de médecins
 * @param nombrePatients    nombre de patients
 * @param tauxAnnulation    pourcentage de rendez-vous annulés (0 à 100)
 * @param parStatut         nombre de rendez-vous par statut (libellé → nombre)
 * @param parSpecialite     nombre de rendez-vous par spécialité
 * @param parMois           nombre de rendez-vous par mois ("2026-09" → nombre), dans l'ordre chronologique
 */
public record Statistiques(long nombreRendezVous,
                           long nombreMedecins,
                           long nombrePatients,
                           double tauxAnnulation,
                           Map<String, Long> parStatut,
                           Map<String, Long> parSpecialite,
                           Map<String, Long> parMois) {
}
