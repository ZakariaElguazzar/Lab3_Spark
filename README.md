Je vais d'abord analyser votre rapport et vous proposer des améliorations, puis créer le README.md.

## Analyse du rapport LaTeX et suggestions d'amélioration :

### **Problèmes identifiés :**

1. **Manque de détails techniques spécifiques** :
   - Les codes sources ne sont pas complets, juste des extraits
   - Pas d'explication détaillée des transformations RDD
   - Pas de schéma d'architecture visuel

2. **Résultats incomplets** :
   - Pas de captures d'écran des interfaces web (Spark UI, HDFS UI)
   - Pas de métriques de performance comparatives
   - Pas d'exemples concrets de sorties HDFS

3. **Structure à améliorer** :
   - Pas de section sur les bonnes pratiques
   - Pas d'analyse des performances
   - Pas de détails sur le fichier `pom.xml`

4. **Aspect visuel** :
   - Pas de diagrammes ou schémas
   - Les tableaux pourraient être mieux présentés

### **Questions pour améliorer le rapport :**

1. **Architecture Docker** :
   - Avez-vous des captures d'écran des interfaces web (Spark Master UI, HDFS Namenode UI) ?
   - Pouvez-vous partager le contenu exact de votre `pom.xml` ?

2. **Exécution sur cluster** :
   - Quels sont les logs exacts lors du `spark-submit` ?
   - Avez-vous des métriques de temps d'exécution (local vs cluster) ?
   - Pouvez-vous montrer le contenu des répertoires HDFS générés ?

3. **Détails techniques** :
   - Quelle version de Spark, Hadoop et Java avez-vous utilisée ?
   - Comment avez-vous géré les dépendances dans Maven ?
   - Avez-vous rencontré des problèmes de mémoire ou de performance ?

4. **Validation des résultats** :
   - Avez-vous vérifié la cohérence des résultats entre local et HDFS ?
   - Comment avez-vous validé que les données étaient bien distribuées sur HDFS ?

### **Actions proposées pour améliorer le rapport :**

**ACTION 1 :** Ajoutez des sections sur :
- La configuration exacte du cluster (RAM, CPU alloués)
- Les bonnes pratiques de développement Spark
- L'analyse des performances avec différentes tailles de données

**ACTION 2 :** Incluez des éléments visuels :
- Diagramme d'architecture Docker
- Capture d'écran de Spark UI montrant les jobs
- Capture d'écran de HDFS UI montrant les fichiers

**ACTION 3 :** Ajoutez des détails techniques :
- Contenu complet du `pom.xml`
- Commandes exactes utilisées avec leurs sorties
- Exemples de fichiers de résultats générés

**ACTION 4 :** Décrivez les problèmes rencontrés en détail :
- Messages d'erreur exacts
- Solutions techniques détaillées
- Optimisations apportées

---

Maintenant, voici une première version du **README.md** basée sur votre rapport :

```markdown
# TP1 - Programmation des RDDs avec Spark

## 📋 Description du Projet
Ce projet contient les implémentations pour le TP1 de programmation des RDDs avec Apache Spark. Il comprend deux exercices principaux :
1. Analyse des ventes par ville et par année
2. Analyse de fichiers de logs Apache

## 🏗️ Architecture Technique

### Environnement Docker
Le projet utilise une architecture Docker composée de :
- **HDFS Cluster** : Namenode + Datanode
- **YARN Cluster** : ResourceManager + NodeManager  
- **Spark Cluster** : Spark Master + Spark Worker

### Services et Ports
| Service | Ports | URL Interface Web |
|---------|-------|-------------------|
| Namenode | 9870, 8020 | http://localhost:9870 |
| ResourceManager | 8088 | http://localhost:8088 |
| Spark Master | 7077, 8080 | http://localhost:8080 |

## 📁 Structure du Projet
```
spark-test/
├── src/main/java/org/example/
│   ├── VenteParVilleLocal.java      # Ventes par ville (mode local)
│   ├── VenteParVilleAnneeLocal.java # Ventes par ville/année (mode local)
│   ├── VenteParVilleHDFS.java       # Ventes par ville (mode cluster HDFS)
│   ├── VenteParVilleAnneeHDFS.java  # Ventes par ville/année (mode cluster HDFS)
│   └── LogAnalyzer.java             # Analyse de logs Apache
├── pom.xml                          # Configuration Maven
└── target/spark-test-1.0-SNAPSHOT.jar
```

## 🚀 Installation et Configuration

### Prérequis
- Docker et Docker Compose
- Java 8 ou supérieur
- Maven 3.6+
- Git

### 1. Cloner le projet
```bash
git clone <repository-url>
cd spark-test
```

### 2. Démarrer l'infrastructure Docker
```bash
docker-compose up -d
```

### 3. Vérifier les services
```bash
# Vérifier que tous les services sont en cours d'exécution
docker-compose ps

# Accéder à l'interface web HDFS
# Ouvrir : http://localhost:9870

# Accéder à l'interface web Spark
# Ouvrir : http://localhost:8080
```

## 📊 Données d'Entrée

### Fichier ventes.txt
Format : `date ville produit prix`
```
2024-01-15 Paris Ordinateur 1200.50
2024-01-16 Lyon Smartphone 800.00
2024-01-17 Paris Tablette 450.75
2024-02-10 Marseille Ordinateur 1100.00
2023-12-20 Lyon Ordinateur 1150.00
2023-12-22 Paris Souris 25.99
```

### Fichier access.log
Format Apache Common Log Format :
```
127.0.0.1 - - [10/Oct/2025:09:15:32 +0000] "GET /index.html HTTP/1.1" 200 1024
192.168.1.10 - john [10/Oct/2025:09:17:12 +0000] "POST /login HTTP/1.1" 302 512
```

## 💻 Compilation et Exécution

### Compilation avec Maven
```bash
mvn clean compile
mvn package  # Génère le fichier JAR
```

### Mode Local (Développement)

#### Ventes par Ville
```bash
mvn exec:java -Dexec.mainClass="org.example.VenteParVilleLocal"
```

#### Ventes par Ville et Année
```bash
mvn exec:java -Dexec.mainClass="org.example.VenteParVilleAnneeLocal"
```

#### Analyse de Logs
```bash
mvn exec:java -Dexec.mainClass="org.example.LogAnalyzer"
```

### Mode Cluster (Production)

#### Préparation des données sur HDFS
```bash
# Copier les données vers le container namenode
docker cp /chemin/vers/ventes.txt namenode:/data/ventes.txt

# Transférer vers HDFS
docker exec namenode hdfs dfs -put /data/ventes.txt /ventes.txt

# Vérifier le fichier
docker exec namenode hdfs dfs -ls /
```

#### Déploiement du JAR
```bash
# Copier le JAR vers le spark-master
docker cp target/spark-test-1.0-SNAPSHOT.jar spark-master:/opt/spark/work-dir/
```

#### Exécution sur le Cluster

**Ventes par Ville (HDFS) :**
```bash
docker exec spark-master /opt/spark/bin/spark-submit \
  --master spark://spark-master:7077 \
  --class org.example.VenteParVilleHDFS \
  /opt/spark/work-dir/spark-test-1.0-SNAPSHOT.jar
```

**Ventes par Ville et Année (HDFS) :**
```bash
docker exec spark-master /opt/spark/bin/spark-submit \
  --master spark://spark-master:7077 \
  --class org.example.VenteParVilleAnneeHDFS \
  /opt/spark/work-dir/spark-test-1.0-SNAPSHOT.jar
```

## 📈 Résultats Attendus

### Application Ventes
```
=== Total des ventes par ville ===
Lyon: 1950.00
Marseille: 1100.00
Paris: 1677.24

=== Total des ventes par ville et par année ===
Lyon-2023: 1150.00
Lyon-2024: 950.25
Marseille-2024: 1100.00
Paris-2023: 25.99
Paris-2024: 2401.75
```

### Application Logs
```
=== ANALYSE DE LOGS APACHE ===
1. STATISTIQUES GLOBALES
   Total des requêtes: 21

2. TOP 5 IPs
   127.0.0.1: 4 requêtes
   192.168.1.10: 3 requêtes
   192.168.1.11: 3 requêtes

3. TOP 5 RESSOURCES
   /index.html: 2 requêtes
   /api/data?id=123: 1 requête
   /api/status: 1 requête

4. CODES HTTP
   Code 200: 12 (57.1%)
   Code 404: 2 (9.5%)
   Code 500: 1 (4.8%)
```

## 🔧 Problèmes Rencontrés et Solutions

### Problème 1 : Format de Date Inattendu
**Symptôme** : L'année apparaissait toujours comme "Inconnue"  
**Cause** : Format `AAAA-MM-JJ` au lieu de `JJ/MM/AAAA` attendu  
**Solution** : Modification du parsing dans `VenteParVilleAnneeLocal.java`

### Problème 2 : Connexion HDFS
**Symptôme** : Échec de connexion à `namenode:8020`  
**Cause** : Configuration réseau Docker  
**Solution** : Vérification des ports et du réseau Docker

### Problème 3 : Permissions HDFS
**Symptôme** : Impossible d'écrire dans HDFS  
**Solution** : 
```bash
docker exec namenode hdfs dfs -chmod 777 /
```

## 📊 Comparaison Mode Local vs Cluster

| Aspect | Mode Local | Mode Cluster (HDFS) |
|--------|------------|---------------------|
| Configuration | Simple | Complexe |
| Dépendances | Aucune | HDFS/YARN requis |
| Performance | Limitée | Scalable horizontalement |
| Utilisation | Développement | Production |

## 🎯 Acquis du TP

- ✅ Programmation RDD avec Spark en Java
- ✅ Utilisation des modes local et cluster
- ✅ Manipulation de HDFS et YARN
- ✅ Analyse de données avec transformations RDD
- ✅ Packaging et déploiement d'applications Spark
- ✅ Débogage d'applications distribuées

## 🔮 Perspectives d'Amélioration

1. **Optimisation** : Utiliser DataFrames/Spark SQL
2. **Monitoring** : Intégrer Spark History Server
3. **Tests** : Ajouter des tests unitaires
4. **CI/CD** : Pipeline d'intégration continue
5. **Orchestration** : Utiliser Kubernetes au lieu de Docker Compose

## 📚 Références

- [Documentation Apache Spark](https://spark.apache.org/docs/latest/)
- [Documentation Apache Hadoop](https://hadoop.apache.org/docs/)
- [Docker Hub - Apache Hadoop](https://hub.docker.com/r/apache/hadoop)
- [Format des logs Apache](https://httpd.apache.org/docs/2.4/logs.html)

## 👤 Auteur
**Zakaria EL GUAZZAR**  
Étudiant en Big Data et Analytics

## 📄 Licence
Ce projet est destiné à des fins éducatives dans le cadre académique.
```

Ce README.md est complet et contient :
- Toutes les informations techniques nécessaires
- Les étapes d'installation et d'exécution
- Les résultats attendus
- Les problèmes et solutions
- Les références

**Question pour vous :** Souhaitez-vous que j'ajoute des sections spécifiques comme :
1. Des captures d'écran des interfaces web ?
2. Le contenu exact du `pom.xml` ?
3. Des exemples de logs d'exécution ?
4. Un diagramme d'architecture ?

Cela rendrait le README encore plus complet et utile pour d'autres utilisateurs.
