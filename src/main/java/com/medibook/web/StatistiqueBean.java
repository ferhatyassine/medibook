package com.medibook.web;

import com.medibook.service.StatistiqueService;
import com.medibook.service.Statistiques;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bean du tableau de bord administrateur (admin/statistiques.xhtml).
 * <p>
 * Les statistiques sont recalculées à chaque affichage de la page (@RequestScoped).
 */
@Named
@RequestScoped
public class StatistiqueBean {

    @Inject
    private StatistiqueService statistiqueService;

    private Statistiques stats;

    @PostConstruct
    public void init() {
        stats = statistiqueService.calculer();
    }

    public Statistiques getStats() {
        return stats;
    }

    // Remarque : Expression Language 5 (Jakarta EE 10) ne sait pas lire les composants
    // d'un record comme des propriétés (#{x.nombre} cherche getNombre()).
    // On expose donc de simples getters qui délèguent au record.

    public long getNombreRendezVous() {
        return stats.nombreRendezVous();
    }

    public long getNombreMedecins() {
        return stats.nombreMedecins();
    }

    public long getNombrePatients() {
        return stats.nombrePatients();
    }

    public double getTauxAnnulation() {
        return stats.tauxAnnulation();
    }

    /**
     * JSF itère plus facilement sur une liste que sur une Map :
     * on convertit donc chaque Map en liste de lignes (libellé, nombre, pourcentage).
     */
    public List<Ligne> getLignesStatut() {
        return enLignes(stats.parStatut());
    }

    public List<Ligne> getLignesSpecialite() {
        return enLignes(stats.parSpecialite());
    }

    public List<Ligne> getLignesMois() {
        return enLignes(stats.parMois());
    }

    private static List<Ligne> enLignes(Map<String, Long> donnees) {
        long max = donnees.values().stream().mapToLong(Long::longValue).max().orElse(0);
        List<Ligne> lignes = new ArrayList<>();
        donnees.forEach((libelle, nombre) ->
                // pourcentage par rapport à la plus grande valeur : sert à dessiner une barre horizontale
                lignes.add(new Ligne(libelle, nombre, max == 0 ? 0 : (int) (nombre * 100 / max))));
        return lignes;
    }

    /**
     * Une ligne d'un mini-graphique en barres.
     * (Classe classique avec getters, et non un record, pour être lisible par EL 5 : #{ligne.libelle}.)
     */
    public static class Ligne {
        private final String libelle;
        private final long nombre;
        private final int pourcentage;

        public Ligne(String libelle, long nombre, int pourcentage) {
            this.libelle = libelle;
            this.nombre = nombre;
            this.pourcentage = pourcentage;
        }

        public String getLibelle() {
            return libelle;
        }

        public long getNombre() {
            return nombre;
        }

        public int getPourcentage() {
            return pourcentage;
        }
    }
}
