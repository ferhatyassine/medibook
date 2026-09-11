package com.medibook.securite;

import com.medibook.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests de la correspondance URL → rôle requis du filtre de sécurité.
 */
class FiltreSecuriteTest {

    @Test
    @DisplayName("Chaque espace privé exige le bon rôle")
    void roleRequis() {
        assertEquals(Role.ADMIN, FiltreSecurite.roleRequisPour("/admin/medecins.xhtml"));
        assertEquals(Role.MEDECIN, FiltreSecurite.roleRequisPour("/medecin/planning.xhtml"));
        assertEquals(Role.PATIENT, FiltreSecurite.roleRequisPour("/patient/reserver.xhtml"));
    }

    @Test
    @DisplayName("Les pages publiques n'exigent aucun rôle")
    void pagesPubliques() {
        assertNull(FiltreSecurite.roleRequisPour("/index.xhtml"));
        assertNull(FiltreSecurite.roleRequisPour("/login.xhtml"));
        assertNull(FiltreSecurite.roleRequisPour("/administration.xhtml"));
    }
}
