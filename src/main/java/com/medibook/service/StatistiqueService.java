package com.medibook.service;

import com.medibook.model.StatutRendezVous;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Calcul des indicateurs du tableau de bord administrateur.
 * <p>
 * Montre deux approches :
 * <ul>
 *   <li>agrégation <b>en base</b> avec {@code GROUP BY} (par statut, par spécialité) ;</li>
 *   <li>agrégation <b>en Java</b> avec les Streams (par mois), plus portable
 *       car les fonctions de date SQL varient d'une base à l'autre.</li>
 * </ul>
 */
@Stateless
public class StatistiqueService {

    @PersistenceContext(unitName = "medibookPU")
    private EntityManager em;

    public Statistiques calculer() {
        // --- Par statut : la requête renvoie des lignes [statut, nombre] ---
        Map<String, Long> parStatut = new LinkedHashMap<>();
        for (StatutRendezVous s : StatutRendezVous.values()) {
            parStatut.put(s.getLibelle(), 0L); // tous les statuts apparaissent, même à 0
        }
        List<Object[]> lignesStatut = em.createNamedQuery("RendezVous.compterParStatut", Object[].class)
                .getResultList();
        long total = 0;
        long annules = 0;
        for (Object[] ligne : lignesStatut) {
            StatutRendezVous statut = (StatutRendezVous) ligne[0];
            long nombre = ((Number) ligne[1]).longValue();
            parStatut.put(statut.getLibelle(), nombre);
            total += nombre;
            if (statut == StatutRendezVous.ANNULE) {
                annules = nombre;
            }
        }

        // --- Par spécialité ---
        Map<String, Long> parSpecialite = new LinkedHashMap<>();
        for (Object[] ligne : em.createNamedQuery("RendezVous.compterParSpecialite", Object[].class).getResultList()) {
            parSpecialite.put((String) ligne[0], ((Number) ligne[1]).longValue());
        }

        // --- Par mois (en Java) : TreeMap = clés triées ("2026-08" avant "2026-09") ---
        List<LocalDateTime> dates = em.createNamedQuery("RendezVous.toutesLesDates", LocalDateTime.class)
                .getResultList();
        Map<String, Long> parMois = dates.stream()
                .collect(Collectors.groupingBy(d -> YearMonth.from(d).toString(),
                        TreeMap::new, Collectors.counting()));

        long nbMedecins = em.createNamedQuery("Medecin.compter", Long.class).getSingleResult();
        long nbPatients = em.createNamedQuery("Patient.compter", Long.class).getSingleResult();

        // Pourcentage arrondi à une décimale
        double taux = total == 0 ? 0 : Math.round(annules * 1000.0 / total) / 10.0;

        return new Statistiques(total, nbMedecins, nbPatients, taux, parStatut, parSpecialite, parMois);
    }
}
