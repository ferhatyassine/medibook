# Guide utilisateur

Ce guide décrit l'utilisation de MediBook, rôle par rôle. Les comptes de démonstration sont listés dans le [README](../README.md#comptes-de-démonstration).

## Visiteur

- **Accueil** (`/medibook/`) : présentation de l'application et comptes de démonstration.
- **Inscription** : bouton *Inscription* en haut à droite. Remplissez le formulaire (nom, prénom, email, téléphone, date de naissance, mot de passe d'au moins 6 caractères). Vous êtes connecté automatiquement après l'inscription.
- **Connexion** : bouton *Connexion*, puis saisissez votre email et votre mot de passe. Vous arrivez ensuite sur la page d'accueil de votre rôle.

## Patient

### Réserver un rendez-vous

Menu *Réserver* :

1. Choisissez une **spécialité** : la liste des médecins se met à jour.
2. Choisissez un **médecin**.
3. Cliquez sur une **date** dans le calendrier : les **créneaux libres** de 30 minutes s'affichent.
4. Cliquez sur un **créneau**, puis indiquez un **motif** si vous le souhaitez.
5. Cliquez sur **Réserver ce créneau**.

Le rendez-vous est créé avec le statut **En attente** : le médecin doit le confirmer.

> Si aucun créneau ne s'affiche, le médecin ne consulte pas ce jour-là, ou tous ses créneaux sont pris. Essayez une autre date.

### Suivre ses rendez-vous

Menu *Mes rendez-vous* :

| Statut | Signification |
|---|---|
| En attente | Demande envoyée, pas encore traitée par le médecin |
| Confirmé | Le médecin a accepté |
| Refusé | Le médecin a refusé : réservez un autre créneau |
| Annulé | Vous avez annulé |
| Terminé | La consultation a eu lieu : le bouton *Compte rendu* affiche les notes et l'ordonnance |

- **Annuler** : possible pour un rendez-vous *En attente* ou *Confirmé* qui n'est pas encore passé. Le créneau redevient libre pour les autres patients.

## Médecin

### Définir ses disponibilités

Menu *Mes disponibilités* :

1. Choisissez le **jour**, l'heure de **début** et l'heure de **fin**.
2. Cliquez sur **Ajouter**.

Exemples : *Lundi 09:00 → 12:00*, puis *Lundi 14:00 → 17:00*. Deux plages d'un même jour ne peuvent pas se chevaucher. La corbeille supprime une plage, sans effet sur les rendez-vous déjà pris.

### Gérer son planning

Menu *Mon planning* :

- Le badge orange indique le nombre de **demandes en attente**.
- **Confirmer** ou **Refuser** une demande en attente.
- **Consultation** (pour un rendez-vous confirmé) : saisissez les notes, obligatoires, et éventuellement une ordonnance. Le rendez-vous passe alors à *Terminé* et le patient peut lire le compte rendu.
- L'interrupteur **Masquer les rendez-vous annulés / refusés** allège la liste.
- Les rendez-vous passés apparaissent en grisé.

## Administrateur

### Tableau de bord

Menu *Statistiques* : nombre de rendez-vous, de médecins et de patients, taux d'annulation, et répartition par statut, par spécialité et par mois.

### Gérer les spécialités

Menu *Spécialités* :

- **Créer** : remplissez le formulaire de gauche, puis *Enregistrer*.
- **Modifier** : crayon ✏️ sur une ligne, modifiez, puis *Enregistrer*. *Annuler* revient en mode création.
- **Supprimer** : corbeille 🗑️. C'est impossible si des médecins sont rattachés à la spécialité.

### Gérer les médecins

Menu *Médecins* :

- **Ajouter** : nom, prénom, email (qui sert d'identifiant de connexion), téléphone, spécialité et mot de passe initial. Communiquez ce mot de passe au médecin.
- **Modifier** : laissez le champ mot de passe vide pour ne pas le changer.
- **Supprimer** : impossible si le médecin a des rendez-vous dans l'historique.

## Déconnexion

Bouton **Déconnexion** en haut à droite. Sans activité pendant 30 minutes, la session expire automatiquement.
