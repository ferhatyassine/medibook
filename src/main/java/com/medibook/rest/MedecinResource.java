package com.medibook.rest;

import com.medibook.model.Medecin;
import com.medibook.rest.dto.CreneauxDTO;
import com.medibook.rest.dto.ErreurDTO;
import com.medibook.rest.dto.MedecinDTO;
import com.medibook.service.MedecinService;
import com.medibook.service.MetierException;
import com.medibook.service.RendezVousService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Ressource REST : les médecins et leurs créneaux libres.
 *
 * <pre>
 * GET /api/medecins                          → tous les médecins
 * GET /api/medecins?specialiteId=2           → médecins d'une spécialité
 * GET /api/medecins/3                        → un médecin
 * GET /api/medecins/3/creneaux?date=2026-09-14 → créneaux libres ce jour-là
 * </pre>
 */
@Path("medecins")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class MedecinResource {

    private static final DateTimeFormatter FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    @Inject
    private MedecinService medecinService;
    @Inject
    private RendezVousService rendezVousService;

    /**
     * @param specialiteId paramètre facultatif de l'URL (?specialiteId=...) ; null s'il est absent
     */
    @GET
    public List<MedecinDTO> lister(@QueryParam("specialiteId") Long specialiteId) {
        List<Medecin> medecins = specialiteId == null
                ? medecinService.tous()
                : medecinService.parSpecialite(specialiteId);
        return medecins.stream().map(MedecinDTO::de).toList();
    }

    /**
     * @param id partie variable du chemin ({id})
     */
    @GET
    @Path("{id}")
    public Response trouver(@PathParam("id") Long id) {
        Medecin m = medecinService.trouver(id);
        if (m == null) {
            // Réponse 404 avec un corps JSON (plutôt que la page d'erreur HTML du serveur)
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErreurDTO("Médecin introuvable : " + id))
                    .build();
        }
        return Response.ok(MedecinDTO.de(m)).build(); // 200 OK + le médecin en JSON
    }

    @GET
    @Path("{id}/creneaux")
    public CreneauxDTO creneaux(@PathParam("id") Long id, @QueryParam("date") String dateTexte) {
        LocalDate date;
        try {
            // LocalDate n'a pas de méthode valueOf(String) : JAX-RS ne peut pas le convertir seul,
            // on reçoit donc un String que l'on analyse nous-mêmes (format ISO : 2026-09-14).
            date = dateTexte == null ? LocalDate.now() : LocalDate.parse(dateTexte);
        } catch (DateTimeParseException e) {
            throw new MetierException("Date invalide, format attendu : AAAA-MM-JJ (ex. 2026-09-14).");
        }
        List<String> libres = rendezVousService.creneauxDisponibles(id, date).stream()
                .map(FORMAT_HEURE::format)
                .toList();
        return new CreneauxDTO(id, date.toString(), libres);
    }
}
