package com.medibook.config;

import com.medibook.model.Medecin;
import com.medibook.model.Patient;
import com.medibook.model.RendezVous;
import com.medibook.model.Role;
import com.medibook.model.Specialite;
import com.medibook.model.Utilisateur;
import com.medibook.service.MedecinService;
import com.medibook.service.RendezVousService;
import com.medibook.service.SpecialiteService;
import com.medibook.service.UtilisateurService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.logging.Logger;

/**
 * Remplit la base avec des données de démonstration au démarrage de l'application.
 * <p>
 * <b>@Singleton</b> : une seule instance pour toute l'application.<br>
 * <b>@Startup</b> : instanciée dès le déploiement (et pas au premier appel).<br>
 * <b>@PostConstruct</b> : la méthode {@link #initialiser()} est appelée juste après la création,
 * dans une transaction.
 * <p>
 * Comptes créés (mot de passe entre parenthèses) :
 * <ul>
 *   <li>admin@medibook.com (admin123)</li>
 *   <li>medecin@medibook.com (Médecine générale), dr.mohamed@medibook.com, dr.yacf@medibook.com,
 *       dr.med@medibook.com (medecin123)</li>
 *   <li>patient@medibook.com (patient123)</li>
 * </ul>
 */
@Singleton
@Startup
public class DonneesDemo {

    private static final Logger LOG = Logger.getLogger(DonneesDemo.class.getName());

    @Inject
    private UtilisateurService utilisateurService;
    @Inject
    private SpecialiteService specialiteService;
    @Inject
    private MedecinService medecinService;
    @Inject
    private RendezVousService rendezVousService;

    @PostConstruct
    public void initialiser() {
        // Si la base contient déjà des utilisateurs (ex. base MySQL persistante), on ne fait rien.
        if (utilisateurService.compter() > 0) {
            LOG.info("Base déjà initialisée : aucune donnée de démonstration ajoutée.");
            return;
        }
        LOG.info("Création des données de démonstration MediBook...");

        // --- Administrateur ---
        utilisateurService.creer(new Utilisateur("Admin", "MediBook", "admin@medibook.com", Role.ADMIN), "admin123");

        // --- Spécialités ---
        Specialite generaliste = specialiteService.creer("Médecine générale", "Consultations courantes, suivi et prévention.");
        Specialite cardio = specialiteService.creer("Cardiologie", "Maladies du cœur et des vaisseaux.");
        Specialite dermato = specialiteService.creer("Dermatologie", "Maladies de la peau, des cheveux et des ongles.");
        Specialite pediatrie = specialiteService.creer("Pédiatrie", "Suivi médical des enfants et des adolescents.");

        // --- Médecins ---
        // Le compte « medecin@medibook.com » est le médecin de démonstration principal
        Medecin generalisteDemo = creerMedecin("Ferhat", "Yassine", "medecin@medibook.com", "514-555-0101", generaliste);
        Medecin cardiologue = creerMedecin("Ferhat", "Mohamed Yassine", "dr.mohamed@medibook.com", "514-555-0102", cardio);
        Medecin dermatologue = creerMedecin("Fer", "Yacine", "dr.yacf@medibook.com", "514-555-0103", dermato);
        Medecin pediatre = creerMedecin("Ferhar", "Med", "dr.med@medibook.com", "514-555-0104", pediatrie);

        // --- Disponibilités : du lundi au vendredi ---
        for (DayOfWeek jour : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            ajouterMatinEtApresMidi(generalisteDemo, jour);
            ajouterMatinEtApresMidi(pediatre, jour);
        }
        for (DayOfWeek jour : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY}) {
            medecinService.ajouterDisponibilite(cardiologue.getId(), jour, LocalTime.of(8, 30), LocalTime.of(12, 30));
        }
        for (DayOfWeek jour : new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.THURSDAY}) {
            medecinService.ajouterDisponibilite(dermatologue.getId(), jour, LocalTime.of(13, 0), LocalTime.of(18, 0));
        }

        // --- Patient de démonstration ---
        Patient patient = new Patient("Amrani", "Sara", "patient@medibook.com");
        patient.setTelephone("438-555-0199");
        patient.setDateNaissance(LocalDate.of(1995, 4, 12));
        utilisateurService.inscrirePatient(patient, "patient123");

        // --- Quelques rendez-vous, aux prochains jours ouvrés ---
        LocalDate j1 = prochainJourOuvre(LocalDate.now().plusDays(1));
        LocalDate j2 = prochainJourOuvre(j1.plusDays(1));

        RendezVous rdv1 = rendezVousService.reserver(patient.getId(), generalisteDemo.getId(),
                LocalDateTime.of(j1, LocalTime.of(10, 0)), "Bilan annuel");
        rendezVousService.confirmer(rdv1.getId(), generalisteDemo.getId());

        rendezVousService.reserver(patient.getId(), pediatre.getId(),
                LocalDateTime.of(j2, LocalTime.of(14, 30)), "Vaccination de rappel");

        LOG.info("Données de démonstration créées.");
    }

    private Medecin creerMedecin(String nom, String prenom, String email, String tel, Specialite specialite) {
        Medecin m = new Medecin(nom, prenom, email, null);
        m.setTelephone(tel);
        return medecinService.creer(m, specialite.getId(), "medecin123");
    }

    private void ajouterMatinEtApresMidi(Medecin medecin, DayOfWeek jour) {
        medecinService.ajouterDisponibilite(medecin.getId(), jour, LocalTime.of(9, 0), LocalTime.of(12, 0));
        medecinService.ajouterDisponibilite(medecin.getId(), jour, LocalTime.of(14, 0), LocalTime.of(17, 0));
    }

    /** Renvoie la date donnée, ou le lundi suivant si elle tombe un week-end. */
    private static LocalDate prochainJourOuvre(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
