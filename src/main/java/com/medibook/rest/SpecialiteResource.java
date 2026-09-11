package com.medibook.rest;

import com.medibook.rest.dto.SpecialiteDTO;
import com.medibook.service.SpecialiteService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Ressource REST : les spécialités.
 * <p>
 * {@code GET /api/specialites} → liste JSON de toutes les spécialités.
 */
@Path("specialites")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON) // toutes les méthodes renvoient du JSON
public class SpecialiteResource {

    @Inject
    private SpecialiteService specialiteService;

    @GET
    public List<SpecialiteDTO> lister() {
        return specialiteService.toutes().stream()
                .map(SpecialiteDTO::de)
                .toList();
    }
}
