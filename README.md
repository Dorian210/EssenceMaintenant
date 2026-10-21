# EssenceMaintenant ⛽

Une application Android (Kotlin Multiplatform) légère et sans serveur pour trouver les stations-service les moins chères autour de vous.

## 🚀 Fonctionnalités

- **Données en direct** : Téléchargement et traitement local des données officielles open data (data.gouv.fr).
- **Visualisation intelligente** : Filtrage automatique selon le niveau de zoom pour ne pas surcharger la carte.
- **Code couleur dynamique** : Classification (Vert / Orange / Rouge) basée sur le carburant sélectionné (SP95, E10, Gazole, etc.).
- **Intégration GPS** : Lancement d'un itinéraire en un clic vers la station choisie dans votre application de navigation préférée.
- **Cartes personnalisables** : Multiples styles de cartes via MapLibre.

## 📱 Télécharger l'application

L'APK précompilé est disponible directement dans la section [Releases](../../releases).

## 🛠️ Développement et Build

### Prérequis

- **Android Studio** (version récente recommandée)
- **JDK 17** (configuré dans Android Studio via *Settings > Build, Execution, Deployment > Build Tools > Gradle*)
- Un appareil Android physique (avec le débogage USB activé) ou un émulateur Android

### Compiler et exécuter localement

1. **Cloner le dépôt :**
   ```bash
   git clone https://github.com/ton-pseudo/EssenceMaintenant.git
   cd EssenceMaintenant
   ```

2. **Ouvrir le projet :**
   Lance Android Studio, choisis **Open** et sélectionne le dossier du projet. Attends la synchronisation Gradle initiale.

3. **Lancer l'application :**
  - Sélectionne le module **`androidApp`** dans le menu déroulant des configurations en haut.
  - Sélectionne ton émulateur ou appareil connecté.
  - Clique sur le bouton **Run** (ou `Shift + F10`).

### Générer l'APK de Debug dans Android Studio

1. Dans le menu supérieur, allez dans **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
2. Attendez la fin de la compilation (indiquée par la notification *Build APK(s): APK(s) generated successfully* en bas à droite).
3. Cliquez sur le lien **locate** dans la notification pour ouvrir le dossier de destination.

L'APK généré se trouve dans :
`androidApp/build/outputs/apk/debug/EssenceMaintenant-debug.apk`

## 📄 Source des données & Licence

- Données de prix des carburants issues de l'API / flux Open Data du gouvernement (*data.gouv.fr*), sous **Licence Ouverte v2.0**.
- Code source distribué sous licence **MIT** (voir le fichier [LICENSE](LICENSE)).

## 🤝 Contribution

Les contributions et *pull requests* sont les bienvenues ! Pour tout bogue ou suggestion d'amélioration, n'hésite pas à ouvrir une *issue*.