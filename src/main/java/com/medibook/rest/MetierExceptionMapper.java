package com.medibook.rest;

import com.medibook.rest.dto.ErreurDTO;
import com.medibook.service.MetierException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Transforme une {@link MetierException} en réponse HTTP 400 (Bad Request) avec un corps JSON.
 * <p>
 * Sans ce « mapper », le client recevrait une erreur 500 peu explicite.
 * {@code @Provider} permet au serveur de le découvrir automatiquement.
 */
@Provider
public class MetierExceptionMapper implements ExceptionMapper<MetierException> {

    @Override
    public Response toResponse(MetierException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErreurDTO(e.getMessage()))
                .build();
    }
}
