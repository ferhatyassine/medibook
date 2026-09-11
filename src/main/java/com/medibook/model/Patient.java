package com.medibook.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

/**
 * Un patient : utilisateur qui peut réserver des rendez-vous.
 * <p>
 * Grâce à l'héritage JOINED, un patient est enregistré à la fois dans
 * la table {@code UTILISATEUR} (données communes) et dans la table
 * {@code PATIENT} (données spécifiques ci-dessous).
 */
@Entity
@Table(name = "PATIENT")
@NamedQuery(name = "Patient.compter", query = "SELECT COUNT(p) FROM Patient p")
public class Patient extends Utilisateur {

    private static final long serialVersionUID = 1L;

    /** Date de naissance : doit être dans le passé. Le type java.time.LocalDate est géré nativement par JPA 3. */
    @Past(message = "La date de naissance doit être dans le passé")
    @Column(name = "DATE_NAISSANCE")
    private LocalDate dateNaissance;

    public Patient() {
        setRole(Role.PATIENT);
    }

    public Patient(String nom, String prenom, String email) {
        super(nom, prenom, email, Role.PATIENT);
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
}
