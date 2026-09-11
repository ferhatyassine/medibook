package com.medibook.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Cycle de vie d'un rendez-vous.
 *
 * <pre>
 *                  ┌──────────► REFUSE   (par le médecin)
 *                  │
 *   EN_ATTENTE ────┼──────────► ANNULE   (par le patient)
 *                  │
 *                  └──► CONFIRME ──► TERMINE (consultation saisie)
 *                           │
 *                           └──────► ANNULE  (par le patient)
 * </pre>
 *
 * Les règles de transition sont centralisées ici, ce qui les rend
 * faciles à tester (voir {@code StatutRendezVousTest}).
 */
public enum StatutRendezVous {

    EN_ATTENTE("En attente", "warning"),
    CONFIRME("Confirmé", "success"),
    REFUSE("Refusé", "danger"),
    ANNULE("Annulé", "secondary"),
    TERMINE("Terminé", "info");

    /** Statuts qui « occupent » un créneau (un autre patient ne peut plus le réserver). */
    public static final Set<StatutRendezVous> OCCUPANT_UN_CRENEAU = EnumSet.of(EN_ATTENTE, CONFIRME, TERMINE);

    private final String libelle;

    /** Couleur du badge PrimeFaces {@code <p:tag severity="...">}. */
    private final String severite;

    StatutRendezVous(String libelle, String severite) {
        this.libelle = libelle;
        this.severite = severite;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getSeverite() {
        return severite;
    }

    /** Le patient peut annuler un rendez-vous en attente ou confirmé. */
    public boolean peutEtreAnnule() {
        return this == EN_ATTENTE || this == CONFIRME;
    }

    /** Le médecin ne peut confirmer ou refuser qu'un rendez-vous en attente. */
    public boolean peutEtreConfirmeOuRefuse() {
        return this == EN_ATTENTE;
    }

    /** La consultation ne peut être saisie que pour un rendez-vous confirmé. */
    public boolean peutEtreTermine() {
        return this == CONFIRME;
    }
}
