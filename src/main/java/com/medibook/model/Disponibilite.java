package com.medibook.model;

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
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Plage horaire hebdomadaire pendant laquelle un médecin reçoit des patients.
 * <p>
 * Exemple : « le LUNDI de 09:00 à 12:00 ». Cette plage est découpée
 * en créneaux de 30 minutes par {@link com.medibook.metier.CalculateurCreneaux}.
 */
@Entity
@Table(name = "DISPONIBILITE")
@NamedQueries({
        @NamedQuery(name = "Disponibilite.parMedecin",
                query = "SELECT d FROM Disponibilite d WHERE d.medecin.id = :medecinId"),
        @NamedQuery(name = "Disponibilite.parMedecinEtJour",
                query = "SELECT d FROM Disponibilite d WHERE d.medecin.id = :medecinId AND d.jour = :jour ORDER BY d.heureDebut"),
        @NamedQuery(name = "Disponibilite.supprimerParMedecin",
                query = "DELETE FROM Disponibilite d WHERE d.medecin.id = :medecinId")
})
public class Disponibilite implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generateurDisponibilite")
    @TableGenerator(name = "generateurDisponibilite", table = "GENERATEUR_ID", pkColumnName = "NOM",
            valueColumnName = "VALEUR", pkColumnValue = "DISPONIBILITE", allocationSize = 1)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "MEDECIN_ID", nullable = false)
    private Medecin medecin;

    /** Jour de la semaine (java.time.DayOfWeek), stocké en texte : "MONDAY", "TUESDAY"... */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DayOfWeek jour;

    @NotNull
    @Column(name = "HEURE_DEBUT", nullable = false)
    private LocalTime heureDebut;

    @NotNull
    @Column(name = "HEURE_FIN", nullable = false)
    private LocalTime heureFin;

    public Disponibilite() {
    }

    public Disponibilite(Medecin medecin, DayOfWeek jour, LocalTime heureDebut, LocalTime heureFin) {
        this.medecin = medecin;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Medecin getMedecin() {
        return medecin;
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
    }

    public DayOfWeek getJour() {
        return jour;
    }

    public void setJour(DayOfWeek jour) {
        this.jour = jour;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Disponibilite autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
