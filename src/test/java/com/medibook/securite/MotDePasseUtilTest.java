package com.medibook.securite;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests du hachage des mots de passe.
 */
class MotDePasseUtilTest {

    @Test
    @DisplayName("Le hachage ne contient pas le mot de passe en clair et suit le format iterations:sel:hash")
    void formatDuHachage() {
        String hache = MotDePasseUtil.hacher("secret123");

        assertFalse(hache.contains("secret123"));
        assertEquals(3, hache.split(":").length);
    }

    @Test
    @DisplayName("Le bon mot de passe est accepté, un mauvais est refusé")
    void verification() {
        String hache = MotDePasseUtil.hacher("secret123");

        assertTrue(MotDePasseUtil.verifier("secret123", hache));
        assertFalse(MotDePasseUtil.verifier("Secret123", hache));
        assertFalse(MotDePasseUtil.verifier("", hache));
    }

    @Test
    @DisplayName("Deux hachages du même mot de passe sont différents (grâce au sel aléatoire)")
    void selAleatoire() {
        assertNotEquals(MotDePasseUtil.hacher("secret123"), MotDePasseUtil.hacher("secret123"));
    }

    @Test
    @DisplayName("Une valeur stockée invalide ne provoque pas d'exception")
    void valeurStockeeInvalide() {
        assertFalse(MotDePasseUtil.verifier("secret123", null));
        assertFalse(MotDePasseUtil.verifier("secret123", "pas-un-hash"));
        assertFalse(MotDePasseUtil.verifier("secret123", "abc:%%%:%%%"));
        assertFalse(MotDePasseUtil.verifier(null, MotDePasseUtil.hacher("secret123")));
    }

    @Test
    @DisplayName("Un mot de passe vide ne peut pas être haché")
    void motDePasseVide() {
        assertThrows(IllegalArgumentException.class, () -> MotDePasseUtil.hacher(""));
        assertThrows(IllegalArgumentException.class, () -> MotDePasseUtil.hacher(null));
    }
}
