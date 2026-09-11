package com.medibook.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Un médecin de la clinique.
 * <p>
 * Relation JPA : chaque médecin a <b>une</b> spécialité, et une spécialité
 * peut concerner <b>plusieurs</b> médecins → {@code @ManyToOne}.
 * La colonne {@code SPECIALITE_ID} de la table {@code MEDECIN} est la clé étrangère.
 */
@Entity
@Table(name = "MEDECIN")
@NamedQueries({
        @NamedQuery(name = "Medecin.tous",
                query = "SELECT m FROM Medecin m ORDER BY m.nom, m.prenom"),
        @NamedQuery(name = "Medecin.parSpecialite",
                query = "SELECT m FROM Medecin m WHERE m.specialite.id = :specialiteId ORDER BY m.nom, m.prenom"),
        @NamedQuery(name = "Medecin.compterParSpecialite",
                query = "SELECT COUNT(m) FROM Medecin m WHERE m.specialite.id = :specialiteId"),
        @NamedQuery(name = "Medecin.compter",
                query = "SELECT COUNT(m) FROM Medecin m")
})
public class Medecin extends Utilisateur {

    private static final long serialVersionUID = 1L;

    /** La spécialité du médecin (chargée automatiquement avec le médecin : ManyToOne est EAGER par défaut). */
    @NotNull(message = "La spécialité est obligatoire")
    @ManyToOne(optional = false)
    @JoinColumn(name = "SPECIALITE_ID", nullable = false)
    private Specialite specialite;

    public Medecin() {
        setRole(Role.MEDECIN);
    }

    public Medecin(String nom, String prenom, String email, Specialite specialite) {
        super(nom, prenom, email, Role.MEDECIN);
        this.specialite = specialite;
    }

    /** Nom affiché dans les listes : "Dr Prénom Nom". */
    public String getNomAffiche() {
        return "Dr " + getNomComplet();
    }

    public Specialite getSpecialite() {
        return specialite;
    }

    public void setSpecialite(Specialite specialite) {
        this.specialite = specialite;
    }
}
