# API REST

L'API REST expose en JSON des données **publiques** : aucune donnée personnelle de patient, aucun email ni mot de passe.

- **URL de base :** `http://localhost:8080/medibook/api`
- **Format :** JSON (UTF-8)
- **Authentification :** aucune (lecture seule)
- **Implémentation :** JAX-RS (package `com.medibook.rest`) et JSON-B pour la conversion en JSON

## Résumé des routes

| Méthode | Route | Description |
|---|---|---|
| GET | `/specialites` | Liste des spécialités |
| GET | `/medecins` | Liste des médecins |
| GET | `/medecins?specialiteId={id}` | Médecins d'une spécialité |
| GET | `/medecins/{id}` | Détail d'un médecin |
| GET | `/medecins/{id}/creneaux?date=AAAA-MM-JJ` | Créneaux libres d'un médecin pour une date |
| GET | `/statistiques` | Statistiques globales |

## Détail

### `GET /specialites`

```bash
curl http://localhost:8080/medibook/api/specialites
```

```json
[
  { "description": "Maladies du cœur et des vaisseaux.", "id": 2, "libelle": "Cardiologie" },
  { "description": "Maladies de la peau, des cheveux et des ongles.", "id": 3, "libelle": "Dermatologie" }
]
```

### `GET /medecins`

Le paramètre `specialiteId` est facultatif.

```bash
curl "http://localhost:8080/medibook/api/medecins?specialiteId=2"
```

```json
[
  { "id": 3, "nom": "Ferhat", "prenom": "Mohamed Yassine", "specialite": "Cardiologie", "specialiteId": 2, "telephone": "514-555-0102" }
]
```

### `GET /medecins/{id}`

```bash
curl http://localhost:8080/medibook/api/medecins/3
```

Réponse `200` : un objet médecin, au même format que ci-dessus. Si l'identifiant n'existe pas, la réponse est **`404 Not Found`** avec le corps `{"erreur": "Médecin introuvable : 99"}`.

### `GET /medecins/{id}/creneaux`

| Paramètre | Obligatoire | Format | Défaut |
|---|---|---|---|
| `date` | non | `AAAA-MM-JJ` (ISO 8601) | aujourd'hui |

```bash
curl "http://localhost:8080/medibook/api/medecins/2/creneaux?date=2026-09-14"
```

```json
{
  "creneaux": ["09:00", "09:30", "10:30", "11:00", "11:30", "14:00", "14:30"],
  "date": "2026-09-14",
  "medecinId": 2
}
```

En cas d'erreur, la réponse est **`400 Bad Request`** avec un message :

```json
{ "erreur": "Date invalide, format attendu : AAAA-MM-JJ (ex. 2026-09-14)." }
```

### `GET /statistiques`

```bash
curl http://localhost:8080/medibook/api/statistiques
```

```json
{
  "nombreMedecins": 4,
  "nombrePatients": 1,
  "nombreRendezVous": 2,
  "parMois": { "2026-09": 2 },
  "parSpecialite": { "Médecine générale": 1, "Pédiatrie": 1 },
  "parStatut": { "En attente": 1, "Confirmé": 1, "Refusé": 0, "Annulé": 0, "Terminé": 0 },
  "tauxAnnulation": 0.0
}
```

> Les identifiants et les dates des exemples dépendent des données présentes au moment de l'appel.

## Gestion des erreurs

| Code | Quand ? | Corps |
|---|---|---|
| `200 OK` | Succès | Données JSON |
| `400 Bad Request` | Règle métier non respectée (`MetierException`) ou paramètre invalide | `{"erreur": "..."}` |
| `404 Not Found` | Ressource inexistante | `{"erreur": "..."}` |

La conversion `MetierException` vers `400` est faite par `MetierExceptionMapper`, une classe `@Provider` qui implémente `ExceptionMapper`.

## Tester l'API

- **Navigateur** : les routes GET s'ouvrent directement, par exemple http://localhost:8080/medibook/api/medecins.
- **curl** : voir les exemples ci-dessus. Sous Windows PowerShell, utilisez `curl.exe` ou `Invoke-RestMethod`.
- **IntelliJ IDEA** : créez un fichier `api.http` avec par exemple `GET http://localhost:8080/medibook/api/medecins` et cliquez sur ▶.
- **Postman** ou **Bruno** : créez une requête GET vers l'URL.

## Pourquoi des DTO ?

Les ressources renvoient des *records* Java (`SpecialiteDTO`, `MedecinDTO`, `CreneauxDTO`) et pas les entités JPA :

1. **Sécurité** : l'entité `Medecin` contient le mot de passe haché, qui ne doit jamais sortir du serveur.
2. **Stabilité** : on peut modifier la base sans changer le format de l'API.
3. **Simplicité** : pas de boucle infinie ni de problème de chargement paresseux lors de la conversion en JSON.
