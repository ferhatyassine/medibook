package com.medibook.securite;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hachage et vérification des mots de passe avec l'algorithme PBKDF2.
 * <p>
 * <b>Pourquoi ne pas stocker le mot de passe en clair ?</b> Si la base de données
 * fuit, les mots de passe ne doivent pas être lisibles.
 * <p>
 * <b>Pourquoi un « sel » ?</b> Un sel aléatoire, différent pour chaque utilisateur,
 * garantit que deux mots de passe identiques donnent deux hachages différents
 * (et empêche l'utilisation de tables pré-calculées).
 * <p>
 * <b>Pourquoi PBKDF2 avec beaucoup d'itérations ?</b> Le calcul est volontairement lent,
 * ce qui rend les attaques par force brute très coûteuses.
 * <p>
 * Format stocké en base : {@code iterations:selEnBase64:hashEnBase64}
 * <p>
 * Cette classe n'utilise que le JDK (package {@code javax.crypto}) : aucune dépendance externe.
 */
public final class MotDePasseUtil {

    private static final String ALGORITHME = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65_536;
    private static final int TAILLE_SEL_OCTETS = 16;
    private static final int TAILLE_HASH_BITS = 256;

    /** Générateur de nombres aléatoires cryptographiquement sûr. */
    private static final SecureRandom RANDOM = new SecureRandom();

    private MotDePasseUtil() {
    }

    /**
     * Hache un mot de passe.
     *
     * @param motDePasse le mot de passe en clair
     * @return la chaîne à stocker en base (format "iterations:sel:hash")
     */
    public static String hacher(String motDePasse) {
        if (motDePasse == null || motDePasse.isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }
        byte[] sel = new byte[TAILLE_SEL_OCTETS];
        RANDOM.nextBytes(sel);
        byte[] hash = pbkdf2(motDePasse.toCharArray(), sel, ITERATIONS);

        Base64.Encoder base64 = Base64.getEncoder();
        return ITERATIONS + ":" + base64.encodeToString(sel) + ":" + base64.encodeToString(hash);
    }

    /**
     * Vérifie qu'un mot de passe saisi correspond au hachage stocké.
     *
     * @param motDePasse le mot de passe saisi par l'utilisateur
     * @param stocke     la valeur stockée en base
     * @return true si le mot de passe est correct
     */
    public static boolean verifier(String motDePasse, String stocke) {
        if (motDePasse == null || stocke == null) {
            return false;
        }
        String[] parties = stocke.split(":");
        if (parties.length != 3) {
            return false; // format inattendu
        }
        try {
            int iterations = Integer.parseInt(parties[0]);
            byte[] sel = Base64.getDecoder().decode(parties[1]);
            byte[] hashAttendu = Base64.getDecoder().decode(parties[2]);
            byte[] hashCalcule = pbkdf2(motDePasse.toCharArray(), sel, iterations);
            // Comparaison en temps constant : évite les attaques « par mesure du temps ».
            return MessageDigest.isEqual(hashAttendu, hashCalcule);
        } catch (IllegalArgumentException e) {
            return false; // nombre ou Base64 invalide
        }
    }

    /** Calcul PBKDF2 proprement dit. */
    private static byte[] pbkdf2(char[] motDePasse, byte[] sel, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(motDePasse, sel, iterations, TAILLE_HASH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHME);
            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            // Ne devrait jamais arriver : PBKDF2WithHmacSHA256 est fourni par tous les JDK modernes.
            throw new IllegalStateException("Algorithme de hachage indisponible : " + ALGORITHME, e);
        } finally {
            spec.clearPassword(); // efface le mot de passe de la mémoire
        }
    }
}
