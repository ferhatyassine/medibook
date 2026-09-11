package com.medibook.model;

/**
 * Les trois rôles possibles d'un utilisateur de MediBook.
 * <p>
 * Le rôle détermine les pages auxquelles l'utilisateur a accès
 * (voir {@link com.medibook.securite.FiltreSecurite}).
 */
public enum Role {

    /** Administrateur de la clinique : gère les médecins, les spécialités et consulte les statistiques. */
    ADMIN("Administrateur"),

    /** Médecin : gère ses disponibilités et ses rendez-vous. */
    MEDECIN("Médecin"),

    /** Patient : réserve et annule ses rendez-vous. */
    PATIENT("Patient");

    /** Libellé lisible affiché dans l'interface. */
    private final String libelle;

    Role(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
