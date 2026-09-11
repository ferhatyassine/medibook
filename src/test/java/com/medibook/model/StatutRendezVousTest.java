package com.medibook.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests des règles de transition entre statuts de rendez-vous.
 */
class StatutRendezVousTest {

    @Test
    @DisplayName("Seuls les rendez-vous en attente ou confirmés peuvent être annulés")
    void annulation() {
        assertTrue(StatutRendezVous.EN_ATTENTE.peutEtreAnnule());
        assertTrue(StatutRendezVous.CONFIRME.peutEtreAnnule());
        assertFalse(StatutRendezVous.REFUSE.peutEtreAnnule());
        assertFalse(StatutRendezVous.ANNULE.peutEtreAnnule());
        assertFalse(StatutRendezVous.TERMINE.peutEtreAnnule());
    }

    @Test
    @DisplayName("Seul un rendez-vous en attente peut être confirmé ou refusé")
    void confirmationOuRefus() {
        assertTrue(StatutRendezVous.EN_ATTENTE.peutEtreConfirmeOuRefuse());
        assertFalse(StatutRendezVous.CONFIRME.peutEtreConfirmeOuRefuse());
        assertFalse(StatutRendezVous.TERMINE.peutEtreConfirmeOuRefuse());
    }

    @Test
    @DisplayName("Seul un rendez-vous confirmé peut être terminé")
    void terminaison() {
        assertTrue(StatutRendezVous.CONFIRME.peutEtreTermine());
        assertFalse(StatutRendezVous.EN_ATTENTE.peutEtreTermine());
        assertFalse(StatutRendezVous.ANNULE.peutEtreTermine());
    }

    @Test
    @DisplayName("Un rendez-vous annulé ou refusé libère le créneau")
    void creneauLibere() {
        assertFalse(StatutRendezVous.OCCUPANT_UN_CRENEAU.contains(StatutRendezVous.ANNULE));
        assertFalse(StatutRendezVous.OCCUPANT_UN_CRENEAU.contains(StatutRendezVous.REFUSE));
        assertTrue(StatutRendezVous.OCCUPANT_UN_CRENEAU.contains(StatutRendezVous.EN_ATTENTE));
    }
}
