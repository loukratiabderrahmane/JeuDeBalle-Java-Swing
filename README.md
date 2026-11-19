# Jeu de balle — mini-projet Java II

Application Java Swing avec balle rouge animée, raquette au clavier et démonstration concrète de `Thread`, `Runnable`, `Thread.sleep()` et `SwingUtilities.invokeLater()`.

## Contenu du dossier

- `JeuBalle.java` : **code source complet**, un fichier, avec des commentaires courts sans accents.
- `JeuBalle.jar` : **application exécutable** avec son icône intégrée.
- `jeu_balle_icon.png` : icône originale personnalisée de l'application.
- `JeuBalleTests.java` : petits tests des collisions (aucun framework nécessaire).
- `lancer_windows.bat` : lancer le JAR facilement sous Windows.
- `compiler_windows.bat` : recompiler et recréer le JAR sous Windows (JDK requis).
- `.gitignore` : fichiers inutiles à ne pas envoyer sur GitHub.

## Lancer sous Windows

Option A : double-cliquer sur **`lancer_windows.bat`**.

Option B : ouvrir un terminal dans ce dossier et exécuter :

```powershell
java -jar JeuBalle.jar
```

Java 11 ou plus récent doit être installé (`java -version`). Le JAR est compilé pour Java 11+ et ne comprend pas une JVM et n'est pas un `.exe` autonome.

## Lancer le code source dans VS Code

Ouvrir le dossier entier avec VS Code, puis utiliser le terminal :

```powershell
javac -encoding UTF-8 JeuBalle.java
java JeuBalle
```

Pour vérifier les collisions :

```powershell
javac -encoding UTF-8 JeuBalle.java JeuBalleTests.java
java JeuBalleTests
```

Pour recréer le JAR : double-cliquer sur `compiler_windows.bat` ou exécuter ses commandes dans le terminal.

## Commandes du jeu

| Contrôle | Action |
| --- | --- |
| Démarrer | Nouvelle partie : crée une balle, replace la raquette, remet les rebonds à zéro |
| Pause | Arrête temporairement le mouvement |
| Reprendre | Repart depuis la position précédente |
| Flèche gauche / droite | Déplace la raquette |
| Slider Vitesse | Ajuste la vitesse (1–12) |

Une collision avec la raquette fait remonter la balle et ajoute 1 point au compteur **Rebonds**. Quand la balle touche le bas de l'écran sans rebond, la partie est perdue.

**Attention aux compteurs :** dans cette version, **une seule balle est active à la fois**. « Total généré » correspond au nombre de nouvelles parties lancées, et non au nombre de balles simultanées. C'est une version fidèle aux fonctionnalités développées au fil des séances, mais pas une garantie de reproduction exacte d'une vidéo ou de consignes professorales non confirmées.

## Technologies du cours

- `JFrame`, `JPanel`, `JButton`, `JSlider`, `JLabel`
- `Graphics2D`, `paintComponent()`, `repaint()`
- `KeyStroke`, `InputMap`, `ActionMap`, `ActionListener`, `ChangeListener`
- `Thread`, `Runnable`, `start()`, `run()`, `sleep()`
- `volatile`, `AtomicBoolean`, `SwingUtilities.invokeLater()` pour séparer la boucle et les modifications Swing

## Notes d'architecture

Le thread secondaire règle la cadence. Le déplacement, les collisions et les dessins sont gérés sur le thread graphique Swing (EDT). `AtomicBoolean` évite qu'une file d'animations en attente s'accumule si l'interface est temporairement occupée. La détection de collision se fait par la distance entre un cercle et un rectangle, ce qui rend les contacts sur les côtés de la raquette plus fiables que la comparaison des simples boîtes englobantes.
