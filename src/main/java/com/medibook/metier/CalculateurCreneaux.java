package com.medibook.metier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Logique « pure » de calcul des créneaux de rendez-vous.
 * <p>
 * Cette classe ne dépend ni de la base de données, ni du serveur :
 * elle reçoit des données et renvoie un résultat. On peut donc la tester
 * très facilement avec JUnit (voir {@code CalculateurCreneauxTest}).
 * C'est une bonne pratique : isoler les règles métier de la technique.
 */
public final class CalculateurCreneaux {

    /** Durée d'un rendez-vous, en minutes. */
    public static final int DUREE_CRENEAU_MINUTES = 30;

    /** Classe utilitaire : on empêche son instanciation. */
    private CalculateurCreneaux() {
    }

    /**
     * Découpe une plage horaire en créneaux de {@value #DUREE_CRENEAU_MINUTES} minutes.
     * <p>
     * Exemple : [09:00 - 10:15[ donne 09:00 et 09:30
     * (10:00 est exclu car le rendez-vous se terminerait à 10:30, après la fin de la plage).
     *
     * @param plage la plage à découper
     * @return la liste des heures de début de créneau, dans l'ordre
     */
    public static List<LocalTime> decouper(PlageHoraire plage) {
        List<LocalTime> creneaux = new ArrayList<>();
        LocalTime heure = plage.debut();
        // On ajoute un créneau tant qu'il se termine avant (ou pile à) la fin de la plage.
        // On vérifie aussi que l'heure ne « repasse » pas par minuit (ex. plage jusqu'à 23:59).
        while (!heure.plusMinutes(DUREE_CRENEAU_MINUTES).isAfter(plage.fin())
                && !heure.plusMinutes(DUREE_CRENEAU_MINUTES).isBefore(heure)) {
            creneaux.add(heure);
            heure = heure.plusMinutes(DUREE_CRENEAU_MINUTES);
        }
        return creneaux;
    }

    /**
     * Calcule les créneaux encore libres pour un médecin, un jour donné.
     *
     * @param plages          les disponibilités du médecin ce jour-là
     * @param date            le jour demandé
     * @param heuresOccupees  les dates/heures des rendez-vous déjà pris
     * @param maintenant      l'instant présent (passé en paramètre pour pouvoir le fixer dans les tests)
     * @return les heures libres, triées et sans doublon
     */
    public static List<LocalTime> creneauxLibres(Collection<PlageHoraire> plages,
                                                 LocalDate date,
                                                 Collection<LocalDateTime> heuresOccupees,
                                                 LocalDateTime maintenant) {
        // Un Set permet de tester « est-ce occupé ? » en temps constant.
        Set<LocalDateTime> occupees = new HashSet<>(heuresOccupees);

        List<LocalTime> libres = new ArrayList<>();
        for (PlageHoraire plage : plages) {
            for (LocalTime heure : decouper(plage)) {
                LocalDateTime creneau = LocalDateTime.of(date, heure);
                boolean dansLeFutur = creneau.isAfter(maintenant);
                if (dansLeFutur && !occupees.contains(creneau) && !libres.contains(heure)) {
                    libres.add(heure);
                }
            }
        }
        libres.sort(null); // null = ordre naturel (chronologique pour LocalTime)
        return libres;
    }

    /**
     * Vérifie qu'une nouvelle plage ne chevauche aucune plage existante.
     *
     * @return true si la nouvelle plage est compatible avec toutes les autres
     */
    public static boolean estCompatible(PlageHoraire nouvelle, Collection<PlageHoraire> existantes) {
        return existantes.stream().noneMatch(nouvelle::chevauche);
    }
}
