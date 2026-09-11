package com.medibook.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Compte rendu rédigé par le médecin à la fin d'un rendez-vous.
 * <p>
 * Elle est rattachée à un {@link RendezVous} (relation {@code @OneToOne}
 * déclarée côté RendezVous).
 */
@Entity
@Table(name = "CONSULTATION")
public class Consultation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generateurConsultation")
    @TableGenerator(name = "generateurConsultation", table = "GENERATEUR_ID", pkColumnName = "NOM",
            valueColumnName = "VALEUR", pkColumnValue = "CONSULTATION", allocationSize = 1)
    private Long id;

    /** Notes du médecin. {@code @Lob} = texte long (CLOB en base). */
    @NotBlank(message = "Les notes de consultation sont obligatoires")
    @Lob
    @Column(nullable = false)
    private String notes;

    /** Ordonnance (facultative). */
    @Lob
    private String ordonnance;

    @Column(name = "DATE_CONSULTATION", nullable = false)
    private LocalDateTime dateConsultation;

    public Consultation() {
    }

    public Consultation(String notes, String ordonnance) {
        this.notes = notes;
        this.ordonnance = ordonnance;
        this.dateConsultation = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getOrdonnance() {
        return ordonnance;
    }

    public void setOrdonnance(String ordonnance) {
        this.ordonnance = ordonnance;
    }

    public LocalDateTime getDateConsultation() {
        return dateConsultation;
    }

    public void setDateConsultation(LocalDateTime dateConsultation) {
        this.dateConsultation = dateConsultation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Consultation autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
