import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * Beschreibung
 *
 * @version 1.0 vom 12.05.2025
 * @author Paul & Julian (& Kyo)
 */

public class FlappyBird extends Application {

  // Anfang Attribute
  private static Rectangle bird = new Rectangle(); // Vogel als Rechteck initialisieren
  private static ArrayList<Rectangle> obstacles = new ArrayList<>(); // Liste der Hindernisse

  private static double acceleration = 800; // Beschleunigungsvariabel
  private static final double GAME_SPEED = 3; // Geschwindigkeit der Hindernisse
  private static double velocity = 150; // Geschwindigkeitsvariabel
  private static double score = 0; // Score-Wert
  private static double obstacleOffset = 0; // Abstand


  private static AnimationTimer animationTimer; // Animation Timer, zur berechnung von Bewegung und Animatioen
  private long lastTime = 0; // Vorherige Zeit zur Berechnung von deltaTime (Spielzeit)

  private static boolean isRunning; // Boolean, ob das Spiel läuft
  // Ende Attribute


  public void start(Stage stage) {
    stage.setTitle("Flappy Bird");

    stage.setHeight(600);
    stage.setWidth(600);

    stage.setResizable(false);

    Pane root = new Pane();
    Scene scene = createScene(root, stage);


    animationTimer = getAnimationTimer(stage, root);
    buildGame(root, stage);


    double newVelocity = velocity * -1.75;
    scene.setOnKeyPressed(event -> {
      switch (event.getCode()) {
        case SPACE:
          // Beispiel: Vogel nach oben bewegen
          velocity = newVelocity;
          break;

        default:
          if (!isRunning) {
            buildGame(root, stage);
          }
          break;
      }
    });


    stage.show();
  } // end of public FlappyBird


  private AnimationTimer getAnimationTimer(Stage stage, Pane root) {
    AnimationTimer animationTimer = new AnimationTimer() {

      @Override
      public void handle(long now) {
        if (lastTime > 0) {
          double deltaTime = (now - lastTime) / 1_000_000_000.0;

          acceleration = 800; // Erhöhte Beschleunigung

         
          double newVelocity = velocity + acceleration * deltaTime;
          double newY = bird.getTranslateY() + newVelocity * deltaTime;

          
          bird.setTranslateY(newY);          
          velocity = newVelocity;

          moveObstacles(root);
          hasDied(stage, root, this, new Text());

        }
        lastTime = now;
      }

    };

    animationTimer.start();
    return animationTimer;
  }


  public void hasDied(Stage stage, Pane root, AnimationTimer animationTimer, Text scoreText) {

    if (bird.getTranslateY() > stage.getHeight() || bird.getTranslateY() < 0) {
      isRunning = false;
      System.out.println("Out of Map!");
    }

    Rectangle obj1 = obstacles.getFirst();
    Rectangle obj2 = obstacles.get(1);

    if(checkCollisions(bird, obj1) || checkCollisions(bird, obj2)) {
        isRunning = false;
    }

      if (!isRunning) {
        createDeathScreen(stage, root);
      }

  }

  public boolean checkCollisions(Rectangle bird, Rectangle obj) {
    boolean intersected = false;

    if(bird.getBoundsInParent().intersects(obj.getBoundsInParent())) {
      intersected = true;
      obj.setFill(Color.RED);
    }


    return intersected;
  }


  public void moveObstacles(Pane root) {
    ArrayList<Rectangle> toRemove = new ArrayList<>();

    for (Rectangle ob : obstacles) {
      ob.setX(ob.getX() - GAME_SPEED);

      if (ob.getX() < 0 - ob.getWidth()) {
        toRemove.add(ob);
      }
    }

    for (Rectangle ob : toRemove) {
      obstacles.remove(ob);
      root.getChildren().remove(ob);
    }
  }


  private void buildGame(Pane root, Stage stage) {
    // Alte Obstacles entfernen
    root.getChildren().removeAll(obstacles);
    root.getChildren().remove(bird);

    obstacles.clear();
    obstacleOffset = 0;


    createBird(root);
    createObstacles(stage, root, 1000);

    createCenteredText(root, stage, "Score: " + score, 48, new Color(.36, .75, .24, 1), -stage.getHeight()/3, -stage.getWidth()/3);

    isRunning = true;
    velocity = 150;

    // Score zurücksetzen
    score = 0;


    bird.setTranslateX(50);
    bird.setTranslateY(50);


    /* Keine Ahnung was das macht tbh

    root.getChildren().removeIf(node ->
            node instanceof Text && node != scoreText ||
                    (node instanceof Rectangle && node != bird));
    */

    // Neue Obstacles spawnen (werden dort bereits zur root hinzugefügt)

    lastTime = 0;
    animationTimer.start();
  }


  public void createBird(Pane root) {
    bird.setHeight(50);
    bird.setWidth(50);

    bird.setFill(new Color(0.40, 0.90, 1.00, .5));
    bird.setX(50);

    root.getChildren().add(bird);
  }


  public void createObstacles(Stage stage, Pane root, double numberOfObstacles) {
    double spacing = bird.getHeight()*4;
    Random random = new Random();

    for (int i = 0; i < numberOfObstacles; i++)  {

      double maxHeight = stage.getHeight() - spacing;
      double heightTop = random.nextDouble(maxHeight);
      double bottomHeight = maxHeight - heightTop;


      int x = (int) (stage.getWidth() + (obstacleOffset * 300)); // 300 Pixel Abstand zwischen Säulenpaaren
      obstacleOffset++;

      Rectangle top = new Rectangle();
      top.setFill(new Color(.55, .55, .55, 1));
      top.setHeight(heightTop);
      top.setWidth(50);
      top.setX(x);
      top.setY(0);

      Rectangle bottom = new Rectangle();
      bottom.setFill(new Color(.55, .55, .55, 1));
      bottom.setHeight(bottomHeight);
      bottom.setWidth(50);
      bottom.setX(x);
      bottom.setY(top.getHeight() + spacing);

      obstacles.add(top);
      obstacles.add(bottom);

      root.getChildren().addAll(top, bottom);
    }
  }

  private void createDeathScreen(Stage stage, Pane root) {
    animationTimer.stop();

    Rectangle overlay = new Rectangle();
    overlay.setFill(new Color(0.00, 0.00, 0.00, 0.5)); // Fehler 5: Korrektur der Schreibweise

    overlay.setX(0);
    overlay.setY(0);

    overlay.setWidth(stage.getWidth());
    overlay.setHeight(stage.getHeight());


    root.getChildren().add(overlay);
    createCenteredText(root, stage, "You Died!", 48, Color.WHITE, 0, 0);
  }

  private void createCenteredText(Pane root, Stage stage, String string, int fontSize, Color color, double verticalOffset, double horizontalOffset) {
    Font font;
    try {
      font = Font.loadFont(getClass().getResourceAsStream("/fonts/font.ttf"), fontSize);
      if (font == null) {
        font = Font.font("Arial", fontSize);
      }
    } catch (Exception e) {
      font = Font.font("Arial", fontSize);
    }

    Text text = new Text(string);
    text.setFont(font);
    text.setTextAlignment(TextAlignment.CENTER);

    text.applyCss();

    double textW = text.getBoundsInLocal().getWidth();
    double textH = text.getBoundsInLocal().getHeight();

    text.setX(((stage.getWidth() - textW) / 2) + horizontalOffset);
    text.setY(((stage.getHeight() - textH) / 2) + verticalOffset);

    text.setFill(color);
    root.getChildren().add(text);
  }


  private Scene createScene(Pane root, Stage stage) {
    Scene scene = new Scene(root, 600, 600);
    scene.setFill(Color.GAINSBORO);

    stage.setScene(scene);
    return scene;
  }


 
  
  // Anfang Methoden
  
  public static void main(String[] args) {

    launch(args);

  } // end of main
  
  // Ende Methoden
} // end of class FlappyBird
