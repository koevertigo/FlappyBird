import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.io.InputStream;

public class FlappyBird extends Application {

  // Texturen laden
  private final Image background_day = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/textures/background-day.png"))); // Hintergrund
  private final Image message = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/textures/message.png"))); // Startbild
  private final Image pipe_green = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/textures/pipe-green.png"))); // Grünes Rohr
  private final Image pipe_red = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/textures/pipe-red.png"))); // Rotes Rohr (optional)
  private final Image bird_flip_up = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/textures/yellowbird-upflap.png"))); // Vogel Flügel oben
  private final Image bird_flip_mid = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/textures/yellowbird-midflap.png"))); // Vogel Flügel Mitte
  private final Image bird_flip_down = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/textures/yellowbird-downflap.png"))); // Vogel Flügel unten
  private final Image gameover = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/textures/gameover.png"))); // Gameover-Bild

  // Attribute
  private static ImageView bird; // Vogel als ImageView
  private static double acceleration = 800; // Beschleunigung nach unten (Schwerkraft)
  private static final double GAME_SPEED = 1.5; // Geschwindigkeit der Hindernisse
  private static double velocity = 150; // Vertikale Geschwindigkeit des Vogels
  private static int score = 0; // Punktestand als int
  private static AnimationTimer animationTimer; // AnimationTimer für das Spiel
  private long lastTime = 0; // Letzte Zeit für DeltaTime-Berechnung
  private static boolean isRunning = false; // Gibt an, ob das Spiel läuft
  private final double WIDTH = 600;   // Fensterbreite
  private final double HEIGHT = 800; // Fensterhöhe
  private static final ArrayList<ImageView> obstacles = new ArrayList<>(); // Liste der Hindernisse
  private static Text scoreText; // Textobjekt für Score-Anzeige

  // Für Score-Logik: Merke, welches Hindernis zuletzt gezählt wurde
  private static ImageView lastScoredObstacle = null;

  public void start(Stage stage) {
    stage.setTitle("Flappy Bird"); // Setze Fenstertitel
    stage.setHeight(HEIGHT); // Setze Fensterhöhe
    stage.setWidth(WIDTH); // Setze Fensterbreite
    stage.setResizable(false); // Fenster nicht skalierbar

    Pane root = new Pane(); // Erstelle Root-Container
    root.setPrefSize(WIDTH, HEIGHT); // Setze feste Größe für das Spielfeld
    Scene scene = createScene(root, stage); // Erstelle Szene

    // Hintergrundbild hinzufügen
    ImageView backgroundView = new ImageView(background_day); // Erstelle Hintergrund-ImageView
    backgroundView.setFitHeight(HEIGHT); // Setze Höhe
    backgroundView.setFitWidth(WIDTH); // Setze Breite
    backgroundView.setPreserveRatio(false); // Kein Seitenverhältnis erzwingen
    root.getChildren().add(backgroundView); // Füge Hintergrund hinzu

    // Startbild (message) hinzufügen, scharf und mittig
    ImageView messageView = new ImageView(message); // Erstelle Startbild-ImageView
    messageView.setSmooth(false); // Keine Weichzeichnung, damit es nicht blurry ist
    messageView.setPreserveRatio(true); // Seitenverhältnis beibehalten
    messageView.setFitWidth(300); // Setze eine sinnvolle Breite
    messageView.setX((WIDTH - messageView.getFitWidth()) / 2); // Zentriere horizontal
    messageView.setY((HEIGHT - messageView.getImage().getHeight()) / 2); // Zentriere vertikal
    root.getChildren().add(messageView); // Füge Startbild hinzu

  private void resetGameState() {
    if (gameTimer != null) {
      gameTimer.stop();
    }
    obstacles.clear();
    score = 0;
    birdVelocity = 150;
    lastFrameTime = 0;
    lastScoredObstacle = null;
  }

  private void setupGameScene() {
    gameRoot.getChildren().clear();
    addBackground();
  }

  private void createBird() {
    bird = new ImageView(birdMidFlap);
    bird.setFitWidth(BIRD_SIZE);
    bird.setFitHeight(BIRD_SIZE);
    bird.setTranslateX(50);
    bird.setTranslateY(50);
    gameRoot.getChildren().add(bird);
  }

  private void createInitialObstacles() {
    createObstacles(INITIAL_OBSTACLES, WINDOW_WIDTH);
  }

  private void createObstacles(int count, double startX) {
    double currentX = startX;

    // If obstacles exist, start from the last obstacle's position
    if (!obstacles.isEmpty()) {
      currentX = obstacles.getLast().getX() + PIPE_SPACING;
    }

    for (int i = 0; i < count; i++) {
      double maxTopHeight = WINDOW_HEIGHT - PIPE_GAP;
      double topHeight = random.nextDouble() * maxTopHeight;
      double bottomHeight = maxTopHeight - topHeight;

      // Create top pipe (flipped)
      ImageView topPipe = createPipe(currentX, 0, topHeight, true);

      // Create bottom pipe
      ImageView bottomPipe = createPipe(currentX, topHeight + PIPE_GAP, bottomHeight, false);

      obstacles.add(topPipe);
      obstacles.add(bottomPipe);
      gameRoot.getChildren().addAll(topPipe, bottomPipe);

      currentX += PIPE_SPACING;
    }
  }

  private ImageView createPipe(double x, double y, double height, boolean flipped) {
    ImageView pipe = new ImageView(pipeGreen);
    pipe.setFitWidth(PIPE_WIDTH);
    pipe.setFitHeight(height);
    pipe.setX(x);
    pipe.setY(y);
    if (flipped) {
      pipe.setScaleY(-1);
    }
    return pipe;
  }

  private void setupScoreDisplay() {
    scoreText = new Text("Score: 0");
    setupScoreFont();
    scoreText.setFill(Color.WHITE);
    scoreText.setStroke(Color.BLACK);
    scoreText.setStrokeWidth(2);
    scoreText.setX(24);
    scoreText.setY(60);
    gameRoot.getChildren().add(scoreText);
  }

  private void setupScoreFont() {
    try {
      InputStream fontStream = getClass().getResourceAsStream("/assets/fonts/Greek-Freak.ttf");
      if (fontStream != null) {
        scoreText.setFont(Font.loadFont(fontStream, 44));
      } else {
        scoreText.setFont(Font.font("Consolas", FontWeight.BOLD, 44));
      }
    } catch (Exception e) {
      scoreText.setFont(Font.font("Consolas", FontWeight.BOLD, 44));
    }
    scoreText.setFill(Color.WHITE); // Setze Schriftfarbe
    scoreText.setStroke(Color.BLACK); // Schwarzer Rand für bessere Lesbarkeit
    scoreText.setStrokeWidth(2); // Dicke des Randes
    scoreText.setX(24); // Setze X-Position
    scoreText.setY(60); // Setze Y-Position

    double newVelocity = velocity * -1.75; // Sprungkraft berechnen

    // Tasteneingaben behandeln
    scene.setOnKeyPressed(event -> {
      switch (event.getCode()) {
        case SPACE: // Wenn Leertaste gedrückt
          velocity = newVelocity; // Vogel springt nach oben
          break;
        default: // Bei anderer Taste
          if (!isRunning) { // Wenn Spiel nicht läuft
            buildGame(root, stage); // Starte neues Spiel
          }
          break;
      }
    });

    stage.show(); // Zeige Fenster an
  }

  private AnimationTimer getAnimationTimer(Stage stage, Pane root) {
    AnimationTimer animationTimer = new AnimationTimer() {
      @Override
      public void handle(long now) {
        if (lastTime > 0) { // Wenn nicht der erste Frame
          double deltaTime = (now - lastTime) / 1_000_000_000.0; // Zeitdifferenz berechnen
          acceleration = 800; // Schwerkraft setzen
          double newVelocity = velocity + acceleration * deltaTime; // Neue Geschwindigkeit berechnen
          double newY = bird.getTranslateY() + newVelocity * deltaTime; // Neue Y-Position berechnen

          // Vogel-Animation je nach Geschwindigkeit
          if (velocity < -100) {
            bird.setImage(bird_flip_up); // Flügel oben
          } else if (velocity > 100) {
            bird.setImage(bird_flip_down); // Flügel unten
          } else {
            bird.setImage(bird_flip_mid); // Flügel Mitte
          }

          double rotation = (90 * velocity) / 1500; // Rotation berechnen
          bird.setRotate(rotation); // Rotation setzen

          bird.setTranslateY(newY); // Neue Y-Position setzen
          velocity = newVelocity; // Geschwindigkeit aktualisieren

          moveObstacles(root, stage); // Hindernisse bewegen
          hasDied(stage, root); // Prüfe, ob Vogel gestorben ist

          updateScore(); // Score-Anzeige aktualisieren
        }
        lastTime = now; // Zeit aktualisieren
      }
    };
    animationTimer.start(); // Timer starten
    return animationTimer; // Timer zurückgeben
  }

  public void hasDied(Stage stage, Pane root) {
    if (bird.getTranslateY() > stage.getHeight() || bird.getTranslateY() < 0) { // Prüfe, ob Vogel aus dem Fenster fliegt
      isRunning = false; // Spiel stoppen
      System.out.println("Out of Map!"); // Debug-Ausgabe
    }

    // Prüfe Kollision mit Hindernissen
    if (obstacles.size() >= 2) { // Wenn mindestens zwei Hindernisse vorhanden
      ImageView obj1 = obstacles.get(0); // Erstes Hindernis
      ImageView obj2 = obstacles.get(1); // Zweites Hindernis

      if(checkCollisions(bird, obj1) || checkCollisions(bird, obj2)) { // Prüfe Kollision
        isRunning = false; // Spiel stoppen
      }
    }

    if (!isRunning) { // Wenn Spiel vorbei
      createDeathScreen(stage, root); // Death-Screen anzeigen
    }
  }

  public boolean checkCollisions(ImageView bird, ImageView obj) {
    boolean intersected = false; // Kollision-Flag

    if(bird.getBoundsInParent().intersects(obj.getBoundsInParent())) { // Prüfe Überlappung
      intersected = true; // Kollision erkannt
      obj.setOpacity(0.5); // Hindernis halbtransparent machen (Feedback)
    }

    return intersected; // Rückgabe, ob Kollision vorliegt
  }

  public void moveObstacles(Pane root, Stage stage) {
    ArrayList<ImageView> toRemove = new ArrayList<>(); // Liste für zu entfernende Hindernisse

    // Score nur erhöhen, wenn der Vogel das untere Rohr eines Paares mittig passiert hat
    for (int i = 0; i < obstacles.size(); i += 2) {
      ImageView top = obstacles.get(i);
      ImageView bottom = obstacles.get(i + 1);

      top.setX(top.getX() - GAME_SPEED);
      bottom.setX(bottom.getX() - GAME_SPEED);

      // Score erhöhen, wenn Vogel das Hindernis-Paar passiert (nur einmal pro Paar)
      if (bottom.getX() + bottom.getFitWidth() < bird.getTranslateX() && lastScoredObstacle != bottom) {
        score++;
        lastScoredObstacle = bottom;
      }

      if (top.getX() < 0 - top.getFitWidth()) {
        toRemove.add(top);
        toRemove.add(bottom);
      }
    }

    for (ImageView ob : toRemove) {
      obstacles.remove(ob);
      root.getChildren().remove(ob);
    }
    // Für jedes entfernte Paar ein neues Paar erzeugen
    int pairsToAdd = toRemove.size() / 2;
    if (pairsToAdd > 0) {
      createObstacles(stage, root, pairsToAdd);
      // ScoreText immer im Vordergrund halten
      root.getChildren().remove(scoreText);
      root.getChildren().add(scoreText);
    }
  }

  private void buildGame(Pane root, Stage stage) {
    animationTimer = getAnimationTimer(stage, root); // AnimationTimer initialisieren

    root.getChildren().clear(); // Alle Elemente entfernen
    obstacles.clear(); // Hindernisse zurücksetzen

    // Hintergrundbild hinzufügen
    ImageView backgroundView = new ImageView(background_day); // Hintergrund-ImageView
    backgroundView.setFitHeight(HEIGHT); // Höhe setzen
    backgroundView.setFitWidth(WIDTH); // Breite setzen
    backgroundView.setPreserveRatio(false); // Kein Seitenverhältnis
    root.getChildren().add(backgroundView); // Hinzufügen

    createBird(root); // Vogel erzeugen
    createObstacles(stage, root, 5); // Hindernisse erzeugen

    // Score zurücksetzen und Score-Anzeige hinzufügen (immer zuletzt, damit sie im Vordergrund ist)
    score = 0; // Score auf 0 setzen
    lastScoredObstacle = null; // Letztes gezähltes Hindernis zurücksetzen
    scoreText = new Text("Score: 0"); // Score-Text neu erstellen
    try {
      InputStream fontStream = getClass().getResourceAsStream("/assets/fonts/Greek-Freak.ttf");
      if (fontStream != null) {
        scoreText.setFont(Font.loadFont(fontStream, 44));
      } else {
        scoreText.setFont(Font.font("Consolas", FontWeight.BOLD, 44));
      }
    } catch (Exception e) {
      scoreText.setFont(Font.font("Consolas", FontWeight.BOLD, 44));
    }
    scoreText.setFill(Color.WHITE);
    scoreText.setStroke(Color.BLACK);
    scoreText.setStrokeWidth(2);
    scoreText.setX(24);
    scoreText.setY(60);
    root.getChildren().add(scoreText);

    isRunning = true; // Spiel läuft
    velocity = 150; // Anfangsgeschwindigkeit

    bird.setTranslateX(50); // Vogel X-Position
    bird.setTranslateY(50); // Vogel Y-Position

    lastTime = 0; // Zeit zurücksetzen
    animationTimer.start(); // Timer starten
  }

  public void createBird(Pane root) {
    bird = new ImageView(bird_flip_mid); // Vogel mit mittlerer Flügelstellung
    bird.setFitWidth(50); // Breite setzen
    bird.setFitHeight(50); // Höhe setzen
    bird.setX(50); // X-Position
    bird.setY(50); // Y-Position
    root.getChildren().add(bird); // Vogel hinzufügen
  }

  public void createObstacles(Stage stage, Pane root, double numberOfObstacles) {
    double spacing = bird.getFitHeight() * 8; // Abstand zwischen Rohren
    Random random = new Random(); // Zufallszahlengenerator

    double lastX = stage.getWidth(); // Start-X-Position
    if (!obstacles.isEmpty()) { // Wenn schon Hindernisse existieren
      lastX = obstacles.getLast().getX(); // Letztes Hindernis nehmen
    }

    for (int i = 0; i < numberOfObstacles; i++) { // Für gewünschte Anzahl
      double maxHeight = stage.getHeight() - spacing; // Maximale Höhe für obere Röhre
      double heightTop = random.nextDouble(maxHeight); // Zufällige Höhe oben
      double bottomHeight = maxHeight - heightTop; // Rest für untere Röhre

      double x = (i == 0 && obstacles.isEmpty()) ?
              stage.getWidth() :
              lastX + 500; // Abstand zwischen Hindernissen

      lastX = x; // X-Position aktualisieren

      // Oberes Rohr
      ImageView top = new ImageView(pipe_green); // Oberes Rohr
      top.setFitWidth(100); // Breite setzen
      top.setFitHeight(heightTop); // Höhe setzen
      top.setX(x); // X-Position
      top.setY(0); // Y-Position
      top.setScaleY(-1); // Vertikal spiegeln
      top.setClip(null); // Kein Clipping, damit nichts abgeschnitten wird

      // Unteres Rohr
      ImageView bottom = new ImageView(pipe_green); // Unteres Rohr
      bottom.setFitWidth(100); // Breite setzen
      bottom.setFitHeight(bottomHeight); // Höhe setzen
      bottom.setX(x); // X-Position
      bottom.setY(heightTop + spacing); // Y-Position
      bottom.setClip(null); // Kein Clipping

      obstacles.add(top); // Oberes Rohr zur Liste
      obstacles.add(bottom); // Unteres Rohr zur Liste
      root.getChildren().addAll(top, bottom); // Beide Rohre zur Anzeige
    }
  }

  private void createDeathScreen(Stage stage, Pane root) {
    animationTimer.stop(); // Animation stoppen

    Rectangle overlay = new Rectangle(); // Overlay für Transparenz
    overlay.setFill(new Color(0.00, 0.00, 0.00, 0.5)); // Schwarzes Overlay halbtransparent
    overlay.setX(0); // X-Position
    overlay.setY(0); // Y-Position
    overlay.setWidth(stage.getWidth()); // Breite
    overlay.setHeight(stage.getHeight()); // Höhe
    root.getChildren().add(overlay); // Overlay hinzufügen

    // Gameover-Bild anzeigen
    ImageView gameoverView = new ImageView(gameover); // Gameover-ImageView
    gameoverView.setFitWidth(300); // Breite setzen
    gameoverView.setPreserveRatio(true); // Seitenverhältnis beibehalten
    gameoverView.setX((stage.getWidth() - 300) / 2); // Zentrieren
    gameoverView.setY(stage.getHeight() / 2 - 150); // Y-Position setzen
    root.getChildren().add(gameoverView); // Gameover-Bild hinzufügen

  }

  private void updateScore() {
    scoreText.setText("Score: " + score); // Score-Text aktualisieren
  }

  private Scene createScene(Pane root, Stage stage) {
    Scene scene = new Scene(root, 600, 600); // Szene mit Root-Container
    stage.setScene(scene); // Szene dem Fenster zuweisen
    return scene; // Szene zurückgeben
  }

  public static void main(String[] args) {
    launch(args); // JavaFX-Anwendung starten
  }
}
