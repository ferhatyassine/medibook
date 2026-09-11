package com.medibook.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;

/**
 * Spécialité médicale (Cardiologie, Pédiatrie, ...).
 */
@Entity
@Table(name = "SPECIALITE")
@NamedQueries({
        @NamedQuery(name = "Specialite.toutes",
                query = "SELECT s FROM Specialite s ORDER BY s.libelle"),
        @NamedQuery(name = "Specialite.parLibelle",
                query = "SELECT s FROM Specialite s WHERE LOWER(s.libelle) = LOWER(:libelle)")
})
public class Specialite implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generateurSpecialite")
    @TableGenerator(name = "generateurSpecialite", table = "GENERATEUR_ID", pkColumnName = "NOM",
            valueColumnName = "VALEUR", pkColumnValue = "SPECIALITE", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 80)
    @Column(nullable = false, unique = true, length = 80)
    private String libelle;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    public Specialite() {
    }

    public Specialite(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Specialite autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
