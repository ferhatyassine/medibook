package com.medibook.metier;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Plage horaire simple [debut, fin[ (début inclus, fin exclue).
 * <p>
 * C'est un {@code record} Java : une classe immuable dont le constructeur,
 * les accesseurs ({@code debut()}, {@code fin()}), {@code equals},
 * {@code hashCode} et {@code toString} sont générés automatiquement.
 *
 * @param debut heure de début (incluse)
 * @param fin   heure de fin (exclue)
 */
public record PlageHoraire(LocalTime debut, LocalTime fin) {

    /**
     * Constructeur « compact » : on y place les vérifications.
     * Il est exécuté à chaque création d'une PlageHoraire.
     */
    public PlageHoraire {
        Objects.requireNonNull(debut, "L'heure de début est obligatoire");
        Objects.requireNonNull(fin, "L'heure de fin est obligatoire");
        if (!debut.isBefore(fin)) {
            throw new IllegalArgumentException("L'heure de début doit être avant l'heure de fin");
        }
    }

    /**
     * Deux plages se chevauchent si l'une commence avant la fin de l'autre
     * et inversement. Exemple : [9h-12h[ et [11h-14h[ se chevauchent,
     * mais [9h-12h[ et [12h-14h[ non (elles se touchent seulement).
     */
    public boolean chevauche(PlageHoraire autre) {
        return this.debut.isBefore(autre.fin) && autre.debut.isBefore(this.fin);
    }
}
