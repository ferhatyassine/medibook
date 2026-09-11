# Installation et lancement

## 1. Prérequis

| Outil | Version | Vérification |
|---|---|---|
| JDK | **17** (conseillé), ou 21 | `java -version` |
| Maven | 3.8 ou plus récent (inclus dans IntelliJ IDEA) | `mvn -v` |
| Navigateur | Chrome, Firefox ou Edge récent | — |

> **Attention à la version de Java.** Payara 6 fonctionne avec Java 11, 17 et 21. N'utilisez pas une version plus récente (Java 25, par exemple) pour lancer le serveur. Si vous avez plusieurs JDK, choisissez le 17 dans votre IDE ou via la variable `JAVA_HOME`.

Sous Windows, pour définir `JAVA_HOME` pour la session en cours (PowerShell) :

```powershell
$env:JAVA_HOME = "C:\Users\<vous>\.jdks\temurin-17.0.17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

## 2. Méthode 1 (recommandée) : Payara Micro via Maven

Rien à installer : le plugin Maven télécharge Payara Micro automatiquement.

```bash
mvn clean package payara-micro:start
```

| Étape | Ce qui se passe |
|---|---|
| `clean` | Supprime le dossier `target/` |
| `package` | Compile, lance les tests unitaires et crée `target/medibook.war` |
| `payara-micro:start` | Démarre Payara Micro et y déploie le WAR |

Ouvrez ensuite **http://localhost:8080/medibook/**. Pour arrêter, faites `Ctrl + C`.

Pour aller plus vite en sautant les tests :

```bash
mvn clean package -DskipTests payara-micro:start
```

## 3. Méthode 2 : Payara Micro en ligne de commande

1. Construisez le WAR avec `mvn clean package`.
2. Téléchargez **Payara Micro 6** (Community) sur https://www.payara.fish/downloads/ (fichier `payara-micro-6.x.jar`).
3. Lancez :

```bash
java -jar payara-micro-6.2024.6.jar --deploy target/medibook.war
```

L'application est servie sur http://localhost:8080/medibook/ (le nom du WAR donne le chemin).

## 4. Méthode 3 : Payara Server (serveur complet)

1. Téléchargez **Payara Server 6 Community** et décompressez-le, par exemple dans `C:\payara6`.
2. Démarrez le domaine :

```bash
C:\payara6\bin\asadmin start-domain
```

3. Déployez le WAR, au choix :
   - en ligne de commande : `C:\payara6\bin\asadmin deploy target\medibook.war` ;
   - ou avec la console d'administration **http://localhost:4848**, dans *Applications → Deploy*.
4. Ouvrez http://localhost:8080/medibook/.
5. Arrêtez le serveur avec `asadmin stop-domain`.

Cette méthode est aussi celle que les IDE utilisent avec leur intégration serveur (voir [IDE.md](IDE.md)).

## 5. Utiliser MySQL (ou PostgreSQL) au lieu de H2

Par défaut, l'application utilise la base H2 intégrée à Payara, et les données disparaissent à l'arrêt. Pour conserver les données dans MySQL :

### a) Créer la base

```sql
CREATE DATABASE medibook CHARACTER SET utf8mb4;
CREATE USER 'medibook'@'localhost' IDENTIFIED BY 'medibook';
GRANT ALL PRIVILEGES ON medibook.* TO 'medibook'@'localhost';
```

### b) Ajouter le pilote JDBC dans `pom.xml`

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.4.0</version>
</dependency>
```

### c) Déclarer la source de données

Créez `src/main/java/com/medibook/config/SourceDeDonnees.java` :

```java
package com.medibook.config;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.ejb.Singleton;

/**
 * Déclare une source de données MySQL, sans configuration côté serveur.
 */
@DataSourceDefinition(
        name = "java:app/jdbc/medibook",
        className = "com.mysql.cj.jdbc.MysqlDataSource",
        url = "jdbc:mysql://localhost:3306/medibook?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        user = "medibook",
        password = "medibook")
@Singleton
public class SourceDeDonnees {
}
```

### d) Modifier `persistence.xml`

```xml
<jta-data-source>java:app/jdbc/medibook</jta-data-source>
...
<!-- "create" : crée les tables si elles n'existent pas, sans effacer les données -->
<property name="jakarta.persistence.schema-generation.database.action" value="create"/>
```

La classe `DonneesDemo` n'ajoute les données de démonstration que si la base est vide.

### PostgreSQL

Le principe est le même, avec :

- la dépendance `org.postgresql:postgresql:42.7.3` ;
- `className = "org.postgresql.ds.PGSimpleDataSource"` ;
- `url = "jdbc:postgresql://localhost:5432/medibook"`.

## 6. Voir les requêtes SQL

Dans `persistence.xml`, décommentez :

```xml
<property name="eclipselink.logging.level.sql" value="FINE"/>
<property name="eclipselink.logging.parameters" value="true"/>
```

## 7. Problèmes courants

| Symptôme | Cause probable | Solution |
|---|---|---|
| `Address already in use` / port 8080 occupé | Un autre serveur tourne déjà | Arrêtez-le, ou changez le port (voir ci-dessous) |
| `Unsupported class file major version` ou erreur au démarrage de Payara | JDK trop récent | Utilisez le JDK 17 |
| `mvn` n'est pas reconnu | Maven absent du PATH | Lancez les commandes depuis IntelliJ (fenêtre Maven), ou installez Maven |
| Page blanche ou erreur 404 sur `/medibook` | Le déploiement a échoué | Lisez les erreurs dans la console Payara (souvent la ligne `Exception during lifecycle processing`) |
| `ViewExpiredException` | Session expirée (30 min) | L'application vous renvoie vers la connexion : reconnectez-vous |
| Les données disparaissent au redémarrage | Comportement normal avec `drop-and-create` | Passez à MySQL (section 5) |

**Changer le port** avec le plugin Maven : ajoutez dans la `<configuration>` du plugin `payara-micro-maven-plugin` :

```xml
<commandLineOptions>
    <option>
        <key>--port</key>
        <value>8081</value>
    </option>
</commandLineOptions>
```

En ligne de commande : `java -jar payara-micro.jar --port 8081 --deploy target/medibook.war`.
