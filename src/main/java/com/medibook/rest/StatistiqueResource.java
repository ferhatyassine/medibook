package com.medibook.rest;

import com.medibook.service.StatistiqueService;
import com.medibook.service.Statistiques;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Ressource REST : statistiques globales (données agrégées, sans information personnelle).
 * <p>
 * {@code GET /api/statistiques}
 */
@Path("statistiques")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class StatistiqueResource {

    @Inject
    private StatistiqueService statistiqueService;

    @GET
    public Statistiques obtenir() {
        return statistiqueService.calculer();
    }
}
