# Quel IDE utiliser ?

## Recommandation : IntelliJ IDEA

IntelliJ IDEA est le choix conseillé pour ce projet :

- il ouvre directement les projets **Maven** et télécharge les dépendances tout seul ;
- il embarque **Maven** : pas besoin de l'installer séparément ;
- l'autocomplétion Java et le refactoring sont excellents ;
- il reconnaît les annotations Jakarta EE et les expressions JSF (`#{...}`).

> **Quelle édition ?** La version gratuite suffit pour MediBook, car tout se lance via Maven (plugin Payara Micro). L'édition complète (*Ultimate*) ajoute l'intégration des serveurs d'application et l'aide avancée sur JSF et JPA. Elle est **gratuite pour les étudiants** avec une licence éducative JetBrains (https://www.jetbrains.com/community/education/).

### Ouvrir et lancer le projet dans IntelliJ IDEA

1. **Ouvrir le projet** : *File → Open…*, sélectionnez le dossier `medibook` (celui qui contient `pom.xml`), puis *Trust Project*.
2. **Choisir le JDK 17** : *File → Project Structure → Project → SDK*, sélectionnez `temurin-17` (ou *Add SDK → Download JDK → version 17*).
3. **Attendre l'import Maven** : la barre de progression en bas de l'écran indique le téléchargement des dépendances.
4. **Créer la configuration de lancement** :
   - *Run → Edit Configurations… → + → Maven* ;
   - **Name** : `MediBook (Payara Micro)` ;
   - **Run** (ou *Command line*) : `clean package payara-micro:start` ;
   - onglet *Runner* (ou *Java Options*) : **JRE** = JDK 17 ;
   - *OK*.
5. **Lancer** avec le bouton ▶ vert. Quand la console affiche `Payara Micro ... ready`, ouvrez http://localhost:8080/medibook/.
6. **Arrêter** avec le bouton ■ rouge.

**Autres astuces :**

- **Lancer les tests** : clic droit sur `src/test/java` puis *Run 'All Tests'*, ou double-clic sur *Lifecycle → test* dans la fenêtre Maven (à droite).
- **Déboguer** : utilisez le bouton 🐞 sur une configuration Maven `clean package` pour les tests. Pour déboguer le serveur, voir la section « Débogage » plus bas.
- **Après une modification** : arrêtez puis relancez la configuration (le WAR est reconstruit).

## Alternative 1 : Eclipse IDE for Enterprise Java and Web Developers

Gratuit, très utilisé dans les cours de JEE.

1. Téléchargez l'édition **Enterprise Java and Web Developers** sur https://www.eclipse.org/downloads/packages/.
2. Installez **Payara Tools** depuis *Help → Eclipse Marketplace* (recherchez « Payara »).
3. *File → Import → Maven → Existing Maven Projects*, puis choisissez le dossier `medibook`.
4. Pour lancer :
   - soit clic droit sur le projet → *Run As → Maven build…*, avec comme *Goals* `clean package payara-micro:start` ;
   - soit vue *Servers* → *New Server → Payara*, puis clic droit sur le projet → *Run As → Run on Server*.

## Alternative 2 : Apache NetBeans

Gratuit, avec la meilleure intégration « clé en main » de Payara et GlassFish.

1. Téléchargez NetBeans sur https://netbeans.apache.org/.
2. *Tools → Servers → Add Server → Payara Server*, en indiquant le dossier d'un Payara Server 6 décompressé.
3. *File → Open Project*, puis le dossier `medibook` (NetBeans reconnaît le `pom.xml`).
4. Clic droit sur le projet → *Properties → Run → Server : Payara*.
5. Bouton ▶ *Run Project* : NetBeans construit, déploie et ouvre le navigateur.

## Comparatif

| Critère | IntelliJ IDEA | Eclipse EE | NetBeans |
|---|---|---|---|
| Prix | Gratuit (Ultimate gratuite pour les étudiants) | Gratuit | Gratuit |
| Prise en main de Maven | Excellente | Bonne | Excellente |
| Intégration Payara | Via Maven (Ultimate : intégrée) | Plugin Payara Tools | Intégrée |
| Aide JSF / JPA | Très bonne (Ultimate) | Correcte | Bonne |
| Performances | Gourmand en mémoire | Moyen | Léger |

## Débogage du serveur (tous IDE)

Pour mettre des points d'arrêt dans les beans ou les services pendant que l'application tourne :

1. Construisez le WAR avec `mvn clean package`.
2. Lancez Payara Micro en mode debug :

```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar payara-micro-6.2024.6.jar --deploy target/medibook.war
```

3. Dans l'IDE, créez une configuration **Remote JVM Debug** sur `localhost:5005` et démarrez-la.
4. Posez un point d'arrêt, par exemple dans `ReservationBean.reserver()`, puis réservez un rendez-vous dans le navigateur.
