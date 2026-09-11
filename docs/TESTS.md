# Tests

## 1. Tests unitaires (JUnit 5)

Les tests unitaires portent sur la **logique pure**, qui ne demande ni serveur ni base de données. Ils s'exécutent en quelques secondes.

```bash
mvn test
```

| Classe de test | Ce qui est vérifié |
|---|---|
| `CalculateurCreneauxTest` | Découpage des plages en créneaux de 30 min, exclusion des créneaux pris ou passés, tri, chevauchements |
| `MotDePasseUtilTest` | Hachage PBKDF2 : format, vérification, sel aléatoire, valeurs invalides |
| `StatutRendezVousTest` | Transitions autorisées entre statuts |
| `FiltreSecuriteTest` | Correspondance entre URL et rôle requis |

Résultat attendu : `Tests run: 21, Failures: 0, Errors: 0`.

Les tests suivent le schéma **Given / When / Then** et utilisent `@DisplayName` pour afficher une description lisible dans l'IDE.

**Astuce :** `CalculateurCreneaux.creneauxLibres(...)` reçoit l'heure actuelle en paramètre (`maintenant`) au lieu d'appeler `LocalDateTime.now()`. Les tests peuvent ainsi fixer une date et donner toujours le même résultat.

## 2. Scénario de test manuel (bout en bout)

Lancez l'application (`mvn clean package payara-micro:start`), puis suivez ce scénario. Il couvre l'ensemble des fonctionnalités.

| # | Acteur | Action | Résultat attendu |
|---|---|---|---|
| 1 | Visiteur | Ouvrir `/medibook/admin/medecins.xhtml` sans être connecté | Redirection vers la page de connexion |
| 2 | Visiteur | Se connecter avec un mauvais mot de passe | Message « Email ou mot de passe incorrect » |
| 3 | Visiteur | S'inscrire avec un nouvel email | Compte créé, arrivée sur *Réserver* |
| 4 | Patient | Réserver : Médecine générale, Dr Yassine Ferhat, prochain jour ouvré, un créneau | Rendez-vous *En attente* dans *Mes rendez-vous* |
| 5 | Patient | Revenir sur *Réserver*, même médecin et même date | Le créneau réservé n'est plus proposé |
| 6 | Patient | Ouvrir `/medibook/admin/statistiques.xhtml` | Page « Accès refusé » |
| 7 | Médecin | Se connecter avec `medecin@medibook.com` / `medecin123` | Le planning affiche la demande et le badge « 1 demande(s) en attente » |
| 8 | Médecin | Confirmer la demande | Statut *Confirmé* |
| 9 | Médecin | *Consultation* : valider sans notes | Message « Les notes sont obligatoires » |
| 10 | Médecin | Saisir des notes et une ordonnance, puis valider | Statut *Terminé* |
| 11 | Médecin | *Mes disponibilités* : ajouter Lundi 11:00 → 13:00 | Erreur de chevauchement (la plage 09:00 → 12:00 existe déjà) |
| 12 | Patient | Se reconnecter avec le compte créé à l'étape 3, puis *Mes rendez-vous*, bouton *Compte rendu* | Notes et ordonnance affichées |
| 13 | Patient | Se connecter avec `patient@medibook.com` / `patient123`, puis annuler le rendez-vous *En attente* (Dr Med Ferhar, pédiatrie) | Statut *Annulé* |
| 14 | Admin | Se connecter avec `admin@medibook.com` / `admin123` | Tableau de bord avec un taux d'annulation supérieur à 0 |
| 15 | Admin | Supprimer la spécialité « Cardiologie » | Refus : un médecin y est rattaché |
| 16 | Admin | Créer la spécialité « Ophtalmologie », puis un médecin dans cette spécialité | Le médecin apparaît dans la liste et peut se connecter |
| 17 | Tous | Ouvrir `/medibook/api/medecins` | JSON contenant 5 médecins |
| 18 | Tous | Ouvrir `/medibook/api/medecins/1/creneaux?date=abc` | Erreur 400 avec un message JSON |

## 3. Aller plus loin

- **Tests d'intégration** des services EJB avec une vraie base : [Arquillian](https://arquillian.org/) avec Payara embarqué, ou [Testcontainers](https://testcontainers.com/).
- **Tests de l'interface** : [Selenium](https://www.selenium.dev/) ou [Playwright](https://playwright.dev/java/).
- **Couverture de code** : plugin Maven [JaCoCo](https://www.jacoco.org/).
