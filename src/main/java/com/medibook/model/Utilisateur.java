package com.medibook.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entité JPA représentant un utilisateur de l'application.
 * <p>
 * <b>Héritage JPA :</b> on utilise la stratégie {@code JOINED}.
 * La table {@code UTILISATEUR} contient les champs communs (nom, email, mot de passe...)
 * et les tables {@code PATIENT} et {@code MEDECIN} contiennent uniquement les champs
 * spécifiques, reliées par la même clé primaire.
 * <p>
 * Un administrateur est simplement un {@code Utilisateur} avec le rôle {@link Role#ADMIN}.
 * <p>
 * Les annotations {@code jakarta.validation} (Bean Validation) sont vérifiées
 * automatiquement par JSF (dans les formulaires) et par JPA (avant l'enregistrement).
 */
@Entity
@Table(name = "UTILISATEUR")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
        // Requêtes nommées : écrites en JPQL (on manipule des entités, pas des tables)
        @NamedQuery(name = "Utilisateur.parEmail",
                query = "SELECT u FROM Utilisateur u WHERE LOWER(u.email) = LOWER(:email)"),
        @NamedQuery(name = "Utilisateur.compter",
                query = "SELECT COUNT(u) FROM Utilisateur u")
})
public class Utilisateur implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Clé primaire générée automatiquement.
     * <p>
     * Stratégie {@code TABLE} : les compteurs sont stockés dans une table {@code GENERATEUR_ID}
     * (une ligne par entité). Elle fonctionne avec toutes les bases (H2, MySQL, PostgreSQL...).
     * <br>Remarque : on n'utilise pas {@code IDENTITY} car EclipseLink 4.0 (le JPA de Payara 6)
     * génère pour H2 2.x une syntaxe « BIGINT IDENTITY » que cette version de H2 refuse.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generateurUtilisateur")
    @TableGenerator(name = "generateurUtilisateur", table = "GENERATEUR_ID", pkColumnName = "NOM",
            valueColumnName = "VALEUR", pkColumnValue = "UTILISATEUR", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String prenom;

    /** L'email sert d'identifiant de connexion : il doit être unique. */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /**
     * Mot de passe <b>haché</b> (jamais en clair !).
     * Format : "iterations:sel:hash" (voir {@link com.medibook.securite.MotDePasseUtil}).
     */
    @NotBlank
    @Column(name = "MOT_DE_PASSE", nullable = false, length = 200)
    private String motDePasse;

    /** Rôle stocké sous forme de texte ("ADMIN", "MEDECIN", "PATIENT") : plus lisible et plus sûr que l'ordinal. */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Size(max = 20)
    @Column(length = 20)
    private String telephone;

    /** Constructeur sans argument obligatoire pour JPA. */
    public Utilisateur() {
    }

    public Utilisateur(String nom, String prenom, String email, Role role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
    }

    /** Renvoie "Prénom Nom", pratique pour l'affichage. */
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    // ------------------------------------------------------------------
    // Getters / setters (nécessaires à JPA et aux expressions EL de JSF)
    // ------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * Deux entités sont égales si elles ont le même identifiant.
     * (Une entité non encore enregistrée, sans id, n'est égale qu'à elle-même.)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilisateur autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + ", email=" + email + "]";
    }
}
