package com.medibook.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Active JAX-RS (API REST) dans l'application.
 * <p>
 * Toutes les ressources REST (classes annotées {@code @Path}) seront accessibles
 * sous le préfixe {@code /api}, par exemple :
 * {@code http://localhost:8080/medibook/api/specialites}
 * <p>
 * La classe est vide : l'annotation suffit, le serveur découvre tout seul les ressources.
 */
@ApplicationPath("api")
public class JaxRsConfig extends Application {
}
