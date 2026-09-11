package com.medibook.metier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires du calcul des créneaux.
 * <p>
 * Chaque test suit le schéma « Given / When / Then » (Étant donné / Quand / Alors).
 * Ces tests n'ont besoin ni de serveur ni de base de données : ils s'exécutent en quelques millisecondes.
 * Lancement : {@code mvn test}
 */
class CalculateurCreneauxTest {

    /** Une date fixe (un lundi) pour que les tests donnent toujours le même résultat. */
    private static final LocalDate LUNDI = LocalDate.of(2030, 1, 7);

    /** « Maintenant » fixé la veille : tous les créneaux du lundi sont dans le futur. */
    private static final LocalDateTime LA_VEILLE = LUNDI.minusDays(1).atTime(12, 0);

    private static LocalTime h(int heure, int minute) {
        return LocalTime.of(heure, minute);
    }

    @Test
    @DisplayName("Une plage de 3 heures donne 6 créneaux de 30 minutes")
    void decouperPlageDeTroisHeures() {
        List<LocalTime> creneaux = CalculateurCreneaux.decouper(new PlageHoraire(h(9, 0), h(12, 0)));

        assertEquals(List.of(h(9, 0), h(9, 30), h(10, 0), h(10, 30), h(11, 0), h(11, 30)), creneaux);
    }

    @Test
    @DisplayName("Un créneau qui dépasserait la fin de la plage n'est pas proposé")
    void decouperPlageIncomplete() {
        List<LocalTime> creneaux = CalculateurCreneaux.decouper(new PlageHoraire(h(9, 0), h(10, 15)));

        assertEquals(List.of(h(9, 0), h(9, 30)), creneaux);
    }

    @Test
    @DisplayName("Une plage de moins de 30 minutes ne donne aucun créneau")
    void decouperPlageTropCourte() {
        assertTrue(CalculateurCreneaux.decouper(new PlageHoraire(h(9, 0), h(9, 20))).isEmpty());
    }

    @Test
    @DisplayName("Une plage qui finit à 23:59 ne boucle pas après minuit")
    void decouperPlageJusquaMinuit() {
        List<LocalTime> creneaux = CalculateurCreneaux.decouper(new PlageHoraire(h(23, 0), h(23, 59)));

        assertEquals(List.of(h(23, 0)), creneaux);
    }

    @Test
    @DisplayName("Les créneaux déjà réservés sont retirés")
    void creneauxLibresSansLesHeuresOccupees() {
        List<PlageHoraire> plages = List.of(new PlageHoraire(h(9, 0), h(11, 0)));
        List<LocalDateTime> occupees = List.of(LUNDI.atTime(9, 30), LUNDI.atTime(10, 0));

        List<LocalTime> libres = CalculateurCreneaux.creneauxLibres(plages, LUNDI, occupees, LA_VEILLE);

        assertEquals(List.of(h(9, 0), h(10, 30)), libres);
    }

    @Test
    @DisplayName("Un rendez-vous occupé un AUTRE jour ne bloque pas le créneau")
    void heureOccupeeUnAutreJour() {
        List<PlageHoraire> plages = List.of(new PlageHoraire(h(9, 0), h(10, 0)));
        List<LocalDateTime> occupees = List.of(LUNDI.plusDays(1).atTime(9, 0));

        List<LocalTime> libres = CalculateurCreneaux.creneauxLibres(plages, LUNDI, occupees, LA_VEILLE);

        assertEquals(List.of(h(9, 0), h(9, 30)), libres);
    }

    @Test
    @DisplayName("Les créneaux déjà passés ne sont pas proposés")
    void creneauxPassesExclus() {
        List<PlageHoraire> plages = List.of(new PlageHoraire(h(9, 0), h(11, 0)));
        LocalDateTime maintenant = LUNDI.atTime(9, 45);

        List<LocalTime> libres = CalculateurCreneaux.creneauxLibres(plages, LUNDI, List.of(), maintenant);

        assertEquals(List.of(h(10, 0), h(10, 30)), libres);
    }

    @Test
    @DisplayName("Plusieurs plages : le résultat est trié chronologiquement")
    void plusieursPlagesTriees() {
        // Plage de l'après-midi volontairement placée en premier
        List<PlageHoraire> plages = List.of(
                new PlageHoraire(h(14, 0), h(15, 0)),
                new PlageHoraire(h(9, 0), h(10, 0)));

        List<LocalTime> libres = CalculateurCreneaux.creneauxLibres(plages, LUNDI, List.of(), LA_VEILLE);

        assertEquals(List.of(h(9, 0), h(9, 30), h(14, 0), h(14, 30)), libres);
    }

    @Test
    @DisplayName("Détection des chevauchements de plages")
    void chevauchement() {
        PlageHoraire matin = new PlageHoraire(h(9, 0), h(12, 0));

        assertTrue(matin.chevauche(new PlageHoraire(h(11, 0), h(13, 0))), "11h-13h chevauche 9h-12h");
        assertTrue(matin.chevauche(new PlageHoraire(h(10, 0), h(11, 0))), "une plage incluse chevauche");
        assertFalse(matin.chevauche(new PlageHoraire(h(12, 0), h(14, 0))), "des plages qui se touchent ne se chevauchent pas");

        assertFalse(CalculateurCreneaux.estCompatible(new PlageHoraire(h(8, 0), h(9, 30)), List.of(matin)));
        assertTrue(CalculateurCreneaux.estCompatible(new PlageHoraire(h(14, 0), h(17, 0)), List.of(matin)));
    }

    @Test
    @DisplayName("Une plage dont le début n'est pas avant la fin est refusée")
    void plageInvalide() {
        assertThrows(IllegalArgumentException.class, () -> new PlageHoraire(h(12, 0), h(9, 0)));
        assertThrows(IllegalArgumentException.class, () -> new PlageHoraire(h(9, 0), h(9, 0)));
    }
}
