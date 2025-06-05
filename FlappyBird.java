import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class FlappyBird extends Application {

  // Konstanten für Spielfeldgröße, Schwerkraft, Geschwindigkeit, etc.
  private static final double WINDOW_WIDTH = 600;
  private static final double WINDOW_HEIGHT = 800;
  private static final double GRAVITY = 800; // Beschleunigung nach unten (Pixel/Sekunde^2)
  private static final double GAME_SPEED = 1.5; // Geschwindigkeit, mit der sich die Hindernisse bewegen
  private static final double JUMP_VELOCITY = -262.5; // Geschwindigkeit beim Sprung (negativ = nach oben)
  private static final double BIRD_SIZE = 50; // Größe des Vogels (Breite und Höhe)
  private static final double PIPE_WIDTH = 100; // Breite der Röhren
  private static final double PIPE_GAP = 500; // Abstand zwischen oberer und unterer Röhre
  private static final double PIPE_SPACING = 300; // Abstand zwischen zwei Röhrenpaaren
  private static final int INITIAL_OBSTACLES = 5; // Anzahl der Hindernispaare zu Spielbeginn

  // Texturen für das Spiel (Hintergrund, Vogel, Röhren, etc.)
  private final Image backgroundDay;
  private final Image messageImage;
  private final Image pipeGreen;
  private final Image birdUpFlap;
  private final Image birdMidFlap;
  private final Image birdDownFlap;
  private final Image gameOverImage;

  // Spielobjekte (Vogel, Punktestand, Hindernisse)
  private ImageView bird;
  private Text scoreText;
  private final ArrayList<ImageView> obstacles = new ArrayList<>();

  // Spielstatus-Variablen
  private double birdVelocity = 150; // Aktuelle Geschwindigkeit des Vogels
  private int score = 0; // Punktestand
  private boolean isGameRunning = false; // Gibt an, ob das Spiel läuft
  private AnimationTimer gameTimer; // Haupt-Spielschleife
  private long lastFrameTime = 0; // Zeitstempel des letzten Frames
  private ImageView lastScoredObstacle = null; // Letztes Hindernis, für das ein Punkt vergeben wurde

  // Weitere Komponenten
  private Stage primaryStage;
  private Pane gameRoot;
  private final Random random = new Random();

  // Konstruktor: Lädt alle benötigten Texturen
  public FlappyBird() {
    this.backgroundDay = loadImage("/assets/textures/background-day.png");
    this.messageImage = loadImage("/assets/textures/message.png");
    this.pipeGreen = loadImage("/assets/textures/pipe-green.png");
    this.birdUpFlap = loadImage("/assets/textures/yellowbird-upflap.png");
    this.birdMidFlap = loadImage("/assets/textures/yellowbird-midflap.png");
    this.birdDownFlap = loadImage("/assets/textures/yellowbird-downflap.png");
    this.gameOverImage = loadImage("/assets/textures/gameover.png");
  }

  // Einstiegspunkt der JavaFX-Anwendung
  @Override
  public void start(Stage stage) {
    this.primaryStage = stage;
    setupWindow();
    showMainMenu();
  }

  // Initialisiert das Hauptfenster
  private void setupWindow() {
    primaryStage.setTitle("Flappy Bird");
    primaryStage.setWidth(WINDOW_WIDTH);
    primaryStage.setHeight(WINDOW_HEIGHT);
    primaryStage.setResizable(false);
    primaryStage.show();
  }

  // Zeigt das Hauptmenü mit Hintergrund und Startnachricht
  private void showMainMenu() {
    gameRoot = new Pane();
    gameRoot.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

    addBackground();
    addStartMessage();

    Scene scene = new Scene(gameRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
    setupInputHandlers(scene);
    primaryStage.setScene(scene);
  }

  // Fügt den Hintergrund zum Spielfeld hinzu
  private void addBackground() {
    ImageView background = new ImageView(backgroundDay);
    background.setFitWidth(WINDOW_WIDTH);
    background.setFitHeight(WINDOW_HEIGHT);
    background.setPreserveRatio(false);
    gameRoot.getChildren().add(background);
  }

  // Zeigt die Startnachricht (z.B. "Press Space to Start")
  private void addStartMessage() {
    ImageView startMessage = new ImageView(messageImage);
    startMessage.setSmooth(false);
    startMessage.setPreserveRatio(true);
    startMessage.setFitWidth(300);
    startMessage.setX((WINDOW_WIDTH - startMessage.getFitWidth()) / 2);
    startMessage.setY((WINDOW_HEIGHT - startMessage.getImage().getHeight()) / 2);
    gameRoot.getChildren().add(startMessage);
  }

  // Setzt die Tasteneingaben für das Spiel (Springen, Neustart)
  private void setupInputHandlers(Scene scene) {
    scene.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.SPACE) {
        handleJump();
      } else if (!isGameRunning) {
        startNewGame();
      }
    });
  }

  // Behandelt den Sprung des Vogels (setzt die Geschwindigkeit nach oben)
  private void handleJump() {
    if (isGameRunning) {
      birdVelocity = JUMP_VELOCITY;
    }
  }

  // Startet ein neues Spiel und setzt alle relevanten Variablen zurück
  private void startNewGame() {
    resetGameState();
    setupGameScene();
    createBird();
    createInitialObstacles();
    setupScoreDisplay();
    startGameLoop();
    isGameRunning = true;
  }

  // Setzt den Spielstatus und die Variablen zurück
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

  // Bereitet das Spielfeld für ein neues Spiel vor
  private void setupGameScene() {
    gameRoot.getChildren().clear();
    addBackground();
  }

  // Erstellt das Vogel-Objekt und fügt es zum Spielfeld hinzu
  private void createBird() {
    bird = new ImageView(birdMidFlap);
    bird.setFitWidth(BIRD_SIZE);
    bird.setFitHeight(BIRD_SIZE);
    bird.setTranslateX(50);
    bird.setTranslateY(50);
    gameRoot.getChildren().add(bird);
  }

  // Erstellt die anfänglichen Hindernisse (Röhrenpaare)
  private void createInitialObstacles() {
    createObstacles(INITIAL_OBSTACLES, WINDOW_WIDTH);
  }

  // Erstellt eine bestimmte Anzahl an Hindernispaaren ab einer Startposition
  private void createObstacles(int count, double startX) {
    double currentX = startX;

    // Falls bereits Hindernisse existieren, beginne nach dem letzten Hindernis
    if (!obstacles.isEmpty()) {
      currentX = obstacles.getLast().getX() + PIPE_SPACING;
    }

    for (int i = 0; i < count; i++) {
      double maxTopHeight = WINDOW_HEIGHT - PIPE_GAP;
      double topHeight = random.nextDouble() * maxTopHeight;
      double bottomHeight = maxTopHeight - topHeight;

      // Erstelle obere Röhre (umgedreht)
      ImageView topPipe = createPipe(currentX, 0, topHeight, true);

      // Erstelle untere Röhre
      ImageView bottomPipe = createPipe(currentX, topHeight + PIPE_GAP, bottomHeight, false);

      obstacles.add(topPipe);
      obstacles.add(bottomPipe);
      gameRoot.getChildren().addAll(topPipe, bottomPipe);

      currentX += PIPE_SPACING;
    }
  }

  // Erstellt eine einzelne Röhre (oben oder unten)
  private ImageView createPipe(double x, double y, double height, boolean flipped) {
    ImageView pipe = new ImageView(pipeGreen);
    pipe.setFitWidth(PIPE_WIDTH);
    pipe.setFitHeight(height);
    pipe.setX(x);
    pipe.setY(y);
    if (flipped) {
      pipe.setScaleY(-1); // Spiegelt die Röhre vertikal für die obere Position
    }
    return pipe;
  }

  // Initialisiert die Anzeige für den Punktestand
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

  // Lädt die Schriftart für den Punktestand oder verwendet eine Standardschrift
  private void setupScoreFont() {
    try {
      InputStream fontStream = getClass().getResourceAsStream("/assets/fonts/flappy_bird.ttf");
      if (fontStream != null) {
        scoreText.setFont(Font.loadFont(fontStream, 44));
      } else {
        scoreText.setFont(Font.font("Consolas", FontWeight.BOLD, 44));
      }
    } catch (Exception e) {
      scoreText.setFont(Font.font("Consolas", FontWeight.BOLD, 44));
    }
  }

  // Startet die Haupt-Spielschleife (wird pro Frame aufgerufen)
  private void startGameLoop() {
    gameTimer = new AnimationTimer() {
      @Override
      public void handle(long currentTime) {
        if (lastFrameTime > 0) {
          double deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0;
          updateGame(deltaTime);
        }
        lastFrameTime = currentTime;
      }
    };
    gameTimer.start();
  }

  // Aktualisiert alle Spielobjekte pro Frame
  private void updateGame(double deltaTime) {
    updateBird(deltaTime);
    updateObstacles();
    checkCollisions();
    updateScore();
  }

  // Aktualisiert die Position und Animation des Vogels
  private void updateBird(double deltaTime) {
    // Schwerkraft anwenden
    birdVelocity += GRAVITY * deltaTime;

    // Neue Position berechnen
    double newY = bird.getTranslateY() + birdVelocity * deltaTime;
    bird.setTranslateY(newY);

    // Animation des Vogels je nach Geschwindigkeit anpassen
    updateBirdAnimation();

    // Rotation des Vogels je nach Geschwindigkeit anpassen
    double rotation = (90 * birdVelocity) / 1500;
    bird.setRotate(rotation);
  }

  // Wechselt das Bild des Vogels je nach Flugrichtung
  private void updateBirdAnimation() {
    if (birdVelocity < -100) {
      bird.setImage(birdUpFlap);
    } else if (birdVelocity > 100) {
      bird.setImage(birdDownFlap);
    } else {
      bird.setImage(birdMidFlap);
    }
  }

  // Bewegt die Hindernisse und prüft, ob sie aus dem Bild verschwunden sind
  private void updateObstacles() {
    ArrayList<ImageView> toRemove = new ArrayList<>();

    // Bewege alle Hindernisse und prüfe, ob ein Punkt erzielt wurde
    for (int i = 0; i < obstacles.size(); i += 2) {
      ImageView topPipe = obstacles.get(i);
      ImageView bottomPipe = obstacles.get(i + 1);

      // Röhren nach links bewegen
      topPipe.setX(topPipe.getX() - GAME_SPEED);
      bottomPipe.setX(bottomPipe.getX() - GAME_SPEED);

      // Prüfen, ob der Vogel das Hindernispaar passiert hat (Punktestand erhöhen)
      if (bottomPipe.getX() + bottomPipe.getFitWidth() < bird.getTranslateX()
              && lastScoredObstacle != bottomPipe) {
        score++;
        lastScoredObstacle = bottomPipe;
      }

      // Röhren zum Entfernen markieren, wenn sie aus dem Bild sind
      if (topPipe.getX() < -topPipe.getFitWidth()) {
        toRemove.add(topPipe);
        toRemove.add(bottomPipe);
      }
    }

    // Entferne alle Röhren, die aus dem Bild sind
    removePipes(toRemove);

    // Erzeuge neue Röhrenpaare, um entfernte zu ersetzen
    int pairsToAdd = toRemove.size() / 2;
    if (pairsToAdd > 0) {
      createObstacles(pairsToAdd, 0); // startX wird anhand der bestehenden Hindernisse berechnet
    }
  }

  // Entfernt die angegebenen Röhren aus dem Spielfeld und der Liste
  private void removePipes(ArrayList<ImageView> pipesToRemove) {
    for (ImageView pipe : pipesToRemove) {
      obstacles.remove(pipe);
      gameRoot.getChildren().remove(pipe);
    }
  }

  // Prüft, ob der Vogel mit Hindernissen oder den Spielfeldrändern kollidiert
  private void checkCollisions() {
    // Kollision mit Spielfeldgrenzen prüfen
    if (bird.getTranslateY() > WINDOW_HEIGHT || bird.getTranslateY() < 0) {
      gameOver();
      return;
    }

    // Kollision mit Röhren prüfen (nur die ersten beiden Röhren)
    if (obstacles.size() >= 2) {
      ImageView firstPipe = obstacles.get(0);
      ImageView secondPipe = obstacles.get(1);

      if (checkPipeCollision(firstPipe) || checkPipeCollision(secondPipe)) {
        gameOver();
      }
    }
  }

  // Prüft, ob der Vogel mit einer bestimmten Röhre kollidiert
  private boolean checkPipeCollision(ImageView pipe) {
    boolean collision = bird.getBoundsInParent().intersects(pipe.getBoundsInParent());
    if (collision) {
      pipe.setOpacity(0.5); // Visuelles Feedback bei Kollision
    }
    return collision;
  }

  // Beendet das Spiel und zeigt den Game-Over-Bildschirm an
  private void gameOver() {
    isGameRunning = false;
    gameTimer.stop();
    showGameOverScreen();
  }

  // Zeigt den Game-Over-Bildschirm mit Overlay und Bild
  private void showGameOverScreen() {
    // Halbtransparentes Overlay hinzufügen
    Rectangle overlay = new Rectangle(WINDOW_WIDTH, WINDOW_HEIGHT);
    overlay.setFill(new Color(0, 0, 0, 0.5));
    gameRoot.getChildren().add(overlay);

    // Game-Over-Bild hinzufügen
    ImageView gameOverView = new ImageView(gameOverImage);
    gameOverView.setFitWidth(300);
    gameOverView.setPreserveRatio(true);
    gameOverView.setX((WINDOW_WIDTH - 300) / 2);
    gameOverView.setY(WINDOW_HEIGHT / 2 - 150);
    gameRoot.getChildren().add(gameOverView);
  }

  // Aktualisiert die Anzeige des Punktestands
  private void updateScore() {
    scoreText.setText("Score: " + score);
  }

  // Lädt ein Bild aus dem Ressourcenpfad
  private Image loadImage(String path) {
    return new Image(Objects.requireNonNull(
            getClass().getResourceAsStream(path)
    ));
  }

  // Hauptmethode zum Starten der Anwendung
  public static void main(String[] args) {
    launch(args);
  }
}
