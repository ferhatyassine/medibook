package com.medibook.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Un rendez-vous entre un patient et un médecin à une date/heure donnée.
 * <p>
 * Relations JPA :
 * <ul>
 *   <li>{@code @ManyToOne} vers {@link Patient} : un patient a plusieurs rendez-vous ;</li>
 *   <li>{@code @ManyToOne} vers {@link Medecin} : un médecin a plusieurs rendez-vous ;</li>
 *   <li>{@code @OneToOne} vers {@link Consultation} : le compte rendu, créé à la fin du rendez-vous.
 *       {@code cascade = ALL} : enregistrer le rendez-vous enregistre aussi la consultation.</li>
 * </ul>
 */
@Entity
@Table(name = "RENDEZ_VOUS")
@NamedQueries({
        @NamedQuery(name = "RendezVous.parPatient",
                query = "SELECT r FROM RendezVous r WHERE r.patient.id = :patientId ORDER BY r.dateHeure DESC"),
        @NamedQuery(name = "RendezVous.parMedecin",
                query = "SELECT r FROM RendezVous r WHERE r.medecin.id = :medecinId ORDER BY r.dateHeure"),
        // Heures déjà occupées chez un médecin sur une période (utilisé pour calculer les créneaux libres)
        @NamedQuery(name = "RendezVous.heuresOccupees",
                query = "SELECT r.dateHeure FROM RendezVous r WHERE r.medecin.id = :medecinId "
                        + "AND r.statut IN :statuts AND r.dateHeure >= :debut AND r.dateHeure < :fin"),
        // Le patient a-t-il déjà un rendez-vous (actif) à cette heure-là ?
        @NamedQuery(name = "RendezVous.compterPatientALHeure",
                query = "SELECT COUNT(r) FROM RendezVous r WHERE r.patient.id = :patientId "
                        + "AND r.statut IN :statuts AND r.dateHeure = :dateHeure"),
        @NamedQuery(name = "RendezVous.compterParMedecin",
                query = "SELECT COUNT(r) FROM RendezVous r WHERE r.medecin.id = :medecinId"),
        @NamedQuery(name = "RendezVous.compterParStatut",
                query = "SELECT r.statut, COUNT(r) FROM RendezVous r GROUP BY r.statut"),
        @NamedQuery(name = "RendezVous.compterParSpecialite",
                query = "SELECT r.medecin.specialite.libelle, COUNT(r) FROM RendezVous r "
                        + "GROUP BY r.medecin.specialite.libelle ORDER BY r.medecin.specialite.libelle"),
        @NamedQuery(name = "RendezVous.toutesLesDates",
                query = "SELECT r.dateHeure FROM RendezVous r")
})
public class RendezVous implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generateurRendezVous")
    @TableGenerator(name = "generateurRendezVous", table = "GENERATEUR_ID", pkColumnName = "NOM",
            valueColumnName = "VALEUR", pkColumnValue = "RENDEZ_VOUS", allocationSize = 1)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "PATIENT_ID", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "MEDECIN_ID", nullable = false)
    private Medecin medecin;

    /** Date et heure de début du rendez-vous (durée : 30 minutes). */
    @NotNull
    @Column(name = "DATE_HEURE", nullable = false)
    private LocalDateTime dateHeure;

    @Size(max = 255)
    @Column(length = 255)
    private String motif;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutRendezVous statut = StatutRendezVous.EN_ATTENTE;

    /** Date de création de la demande (remplie automatiquement, voir {@link #avantEnregistrement()}). */
    @Column(name = "DATE_CREATION", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "CONSULTATION_ID")
    private Consultation consultation;

    public RendezVous() {
    }

    public RendezVous(Patient patient, Medecin medecin, LocalDateTime dateHeure, String motif) {
        this.patient = patient;
        this.medecin = medecin;
        this.dateHeure = dateHeure;
        this.motif = motif;
    }

    /**
     * Callback JPA : cette méthode est appelée automatiquement juste avant
     * le premier INSERT en base.
     */
    @PrePersist
    void avantEnregistrement() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutRendezVous.EN_ATTENTE;
        }
    }

    /** Vrai si le rendez-vous est déjà passé (utile pour l'affichage). */
    public boolean isPasse() {
        return dateHeure != null && dateHeure.isBefore(LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Medecin getMedecin() {
        return medecin;
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public StatutRendezVous getStatut() {
        return statut;
    }

    public void setStatut(StatutRendezVous statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RendezVous autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
