# MediBook, gestion de rendez-vous médicaux

MediBook est une application web **Jakarta EE 10** qui gère les rendez-vous d'une clinique :

- les **patients** réservent un créneau auprès d'un médecin et suivent leurs demandes ;
- les **médecins** définissent leurs disponibilités, confirment les rendez-vous et rédigent les comptes rendus ;
- l'**administrateur** gère les médecins et les spécialités, et consulte les statistiques.

Ce projet pédagogique couvre les principales briques de Jakarta EE : JSF (avec PrimeFaces), CDI, EJB, JPA, JAX-RS, Bean Validation et les filtres Servlet.

---

## Sommaire

1. [Fonctionnalités](#fonctionnalités)
2. [Technologies](#technologies)
3. [Démarrage rapide](#démarrage-rapide)
4. [Comptes de démonstration](#comptes-de-démonstration)
5. [Structure du projet](#structure-du-projet)
6. [Documentation détaillée](#documentation-détaillée)

---

## Fonctionnalités

| Rôle | Fonctionnalités |
|---|---|
| Visiteur | Page d'accueil, inscription d'un compte patient, connexion |
| Patient | Réservation en 3 étapes (spécialité → médecin → date et créneau), liste de ses rendez-vous, annulation, lecture du compte rendu |
| Médecin | Planning, confirmation ou refus des demandes, saisie de la consultation (notes et ordonnance), gestion des disponibilités hebdomadaires |
| Admin | Tableau de bord (statistiques), gestion des médecins, gestion des spécialités |
| API REST | Spécialités, médecins, créneaux libres et statistiques en JSON |

Règles métier gérées côté serveur :

- un rendez-vous dure **30 minutes** et doit se trouver dans une plage de disponibilité du médecin ;
- un créneau déjà réservé (en attente, confirmé ou terminé) n'est plus proposé ;
- on ne peut pas réserver dans le passé ni avoir deux rendez-vous à la même heure ;
- deux plages de disponibilité d'un même médecin ne peuvent pas se chevaucher ;
- le cycle de vie d'un rendez-vous est contrôlé : `EN_ATTENTE → CONFIRME → TERMINE`, ou `REFUSE` / `ANNULE`.

## Technologies

| Couche | Technologie |
|---|---|
| Langage | Java 17 |
| Plateforme | Jakarta EE 10 |
| Interface | Jakarta Faces (JSF) 4 et PrimeFaces 14 |
| Logique métier | EJB `@Stateless` / `@Singleton` et CDI |
| Persistance | JPA 3.1 (EclipseLink, fourni par Payara) |
| Base de données | H2 embarquée, sans installation (MySQL ou PostgreSQL possibles) |
| API | JAX-RS et JSON-B |
| Sécurité | Filtre Servlet et mots de passe hachés en PBKDF2 |
| Serveur | Payara Micro 6 (ou Payara Server) |
| Build et tests | Maven, JUnit 5 |

## Démarrage rapide

**Prérequis :** JDK 17 et Maven 3.8 ou plus récent. IntelliJ IDEA embarque déjà Maven.

```bash
# 1. Se placer dans le dossier du projet
cd medibook

# 2. Compiler, tester, puis lancer l'application sur Payara Micro
mvn clean package payara-micro:start
```

Au premier lancement, Maven télécharge les dépendances et Payara Micro, ce qui prend quelques minutes. Attendez ensuite le message `Payara Micro ... ready` dans la console, puis ouvrez :

- l'application : **http://localhost:8080/medibook/**
- l'API REST : http://localhost:8080/medibook/api/specialites

Pour arrêter le serveur, faites `Ctrl + C` dans le terminal.

Pour lancer l'application depuis un IDE, voir [docs/IDE.md](docs/IDE.md). Pour les autres modes de déploiement (Payara Server, MySQL), voir [docs/INSTALLATION.md](docs/INSTALLATION.md).

## Comptes de démonstration

Ces comptes sont créés automatiquement au démarrage par la classe `DonneesDemo` :

| Rôle | Email | Mot de passe |
|---|---|---|
| Administrateur | `admin@medibook.com` | `admin123` |
| Médecin (Médecine générale) | `medecin@medibook.com` | `medecin123` |
| Médecin (Cardiologie) | `dr.mohamed@medibook.com` | `medecin123` |
| Médecin (Dermatologie) | `dr.yacf@medibook.com` | `medecin123` |
| Médecin (Pédiatrie) | `dr.med@medibook.com` | `medecin123` |
| Patient | `patient@medibook.com` | `patient123` |

> Avec la configuration par défaut (`drop-and-create`), la base est réinitialisée à chaque redémarrage.

## Structure du projet

```
medibook/
├── pom.xml                         Configuration Maven (dépendances, plugins)
├── README.md                       Ce fichier
├── docs/                           Documentation détaillée
└── src/
    ├── main/
    │   ├── java/com/medibook/
    │   │   ├── model/              Entités JPA (Utilisateur, Patient, Medecin, RendezVous...)
    │   │   ├── metier/             Logique pure, sans dépendance (calcul des créneaux)
    │   │   ├── service/            EJB : règles métier et transactions
    │   │   ├── securite/           Hachage des mots de passe, session, filtre d'accès
    │   │   ├── web/                Beans JSF (contrôleurs des pages)
    │   │   ├── rest/               API REST (ressources JAX-RS + DTO)
    │   │   └── config/             Données de démonstration
    │   ├── resources/META-INF/
    │   │   └── persistence.xml     Configuration JPA
    │   └── webapp/
    │       ├── WEB-INF/            web.xml, beans.xml, faces-config.xml, gabarit
    │       ├── resources/css/      Feuille de style
    │       ├── patient/            Pages du patient
    │       ├── medecin/            Pages du médecin
    │       ├── admin/              Pages de l'administrateur
    │       └── *.xhtml             Pages publiques (accueil, connexion, inscription)
    └── test/java/                  Tests unitaires JUnit 5
```

## Documentation détaillée

| Document | Contenu |
|---|---|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Architecture en couches, modèle de données, déroulement d'une réservation |
| [docs/INSTALLATION.md](docs/INSTALLATION.md) | Installation, lancement, Payara Server, MySQL et PostgreSQL, problèmes courants |
| [docs/IDE.md](docs/IDE.md) | Choix de l'IDE et configuration (IntelliJ IDEA, Eclipse, NetBeans) |
| [docs/API.md](docs/API.md) | Documentation de l'API REST avec exemples |
| [docs/GUIDE-UTILISATEUR.md](docs/GUIDE-UTILISATEUR.md) | Utilisation de l'application, rôle par rôle |
| [docs/TESTS.md](docs/TESTS.md) | Tests unitaires et scénario de test manuel |
| [docs/requetes-api.http](docs/requetes-api.http) | Requêtes prêtes à lancer dans IntelliJ IDEA |

## Pistes d'amélioration

- Remplacer le filtre maison par **Jakarta Security** (`@CustomFormAuthenticationMechanismDefinition`).
- Envoyer un **email** de confirmation avec Jakarta Mail.
- Sécuriser l'API REST avec des jetons **JWT** (MicroProfile JWT est inclus dans Payara).
- Ajouter des **tests d'intégration** avec Arquillian ou Testcontainers.
- Gérer les migrations de base avec **Flyway**.
- Empaqueter l'application dans une image **Docker** (`payara/micro`).

## Auteur

Projet réalisé par **Mohamed Yassine Ferhat**, sous licence MIT (voir [LICENSE](LICENSE)).
