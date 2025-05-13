import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.time.Year;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * Beschreibung
 *
 * @version 1.0 vom 12.05.2025
 * @author 
 */

public class FlappyBird extends Application {
  // Anfang Attribute
  private static final Rectangle bird = new Rectangle();
  private long lastTime = 0;
  private static final ArrayList<Rectangle> obstacles = new ArrayList<>();
  private static final double[] velocity = {0, 150};
  private static double score = 0;
  private static int obstacleOffset = 0;
  private Text scoreText;

  private static boolean isRunning = true;
  // Ende Attribute
  
  public void start(Stage stage) {

    stage.setTitle("Flappy Bird");

    Pane root = new Pane();

    createBird(root);


    Scene scene = new Scene(root, 600, 600);
    scene.setFill(Color.GAINSBORO);
    stage.setScene(scene);

    stage.show();

    stage.setResizable(false);

    spawnObstacles(stage, root, 1000);
    createScore(root);


    AnimationTimer animationTimer = getAnimationTimer(stage, root);

    double newVelY = velocity[1] * -1.75;

    scene.setOnKeyPressed(event -> {
      switch (event.getCode()) {
        case SPACE:
          // Beispiel: Vogel nach oben bewegen

          velocity[1] = newVelY;

          break;

        default:
          if (!isRunning) {
            restart(root, stage, animationTimer);
          }
          break;
      }
    });


  } // end of public FlappyBird

  private void createScore(Pane root) {
    Font font;
    try {
      font = Font.loadFont(getClass().getResourceAsStream("/fonts/font.ttf"), 48);
      if (font == null) {

        font = Font.font("Arial", 48);
      }
    } catch (Exception e) {
      font = Font.font("Arial", 48);
    }

    scoreText = new Text("Score: " + String.valueOf(score).toCharArray()[0]);

    scoreText.setX(50);
    scoreText.setY(50);

    scoreText.setFont(Font.font(font.getFamily(), 48));
    scoreText.setTextAlignment(TextAlignment.CENTER);
    scoreText.applyCss();
    scoreText.setFill(Color.BLACK);


    root.getChildren().add(scoreText);

  }

  private AnimationTimer getAnimationTimer(Stage stage, Pane root) {
    AnimationTimer animationTimer = new AnimationTimer() {

      @Override
      public void handle(long now) {
        if (lastTime > 0) {
          double deltaTime = (now - lastTime) / 1_000_000_000.0;

          final double xAccel = 0;
          final double yAccel = 800; // Erhöhte Beschleunigung

          double newXVelocity = velocity[0] + xAccel * deltaTime;
          double newYVelocity = velocity[1] + yAccel * deltaTime;

          double newX = bird.getTranslateX() + newXVelocity * deltaTime;
          double newY = bird.getTranslateY() + newYVelocity * deltaTime;

          bird.setTranslateX(newX);
          bird.setTranslateY(newY);

          velocity[0] = newXVelocity;
          velocity[1] = newYVelocity;

          moveObstacles(root, stage);


          hasDied(stage, root, this);


        }

        lastTime = now;
      }

    };

    animationTimer.start();
    return animationTimer;
  }


  public void hasDied(Stage stage, Pane root, AnimationTimer animationTimer) {

    if (bird.getTranslateY() > stage.getHeight() || bird.getTranslateY() < 0) {
      isRunning = false;
      System.out.println("Out of Map!");
    }

      for (Rectangle ob : obstacles) {
        if (obstacles.getFirst() == ob || obstacles.get(1) == ob) {

          if(ob.getX() == bird.getX()) {
            score += .5;
            scoreText.setText("Score: " + String.valueOf(score).toCharArray()[0]);
            System.out.println(score);
          }

          if(bird.getBoundsInParent().intersects(ob.getBoundsInParent())) {
            ob.setFill(Color.RED);
            isRunning = false;
          }
        }
      }

      if (!isRunning) {
      animationTimer.stop();

      Rectangle overlay = new Rectangle();
      overlay.setFill(new Color(0.00, 0.00, 0.00, 0.5)); // Fehler 5: Korrektur der Schreibweise

      overlay.setX(0);
      overlay.setY(0);

      overlay.setWidth(stage.getWidth());
      overlay.setHeight(stage.getHeight());


      Font font;
      try {
        font = Font.loadFont(getClass().getResourceAsStream("/fonts/font.ttf"), 48);
        if (font == null) {

          font = Font.font("Arial", 48);
        }
      } catch (Exception e) {
        font = Font.font("Arial", 48);
      }

      Text title = new Text("You Died!");
      Text subtitle = new Text("Press any key to restart");

      title.setFont(Font.font(font.getFamily(), 48));
      subtitle.setFont(Font.font(font.getFamily(), 32));

      title.setTextAlignment(TextAlignment.CENTER);
      subtitle.setTextAlignment(TextAlignment.CENTER);


      title.applyCss();
      subtitle.applyCss();

      double titleW = title.getBoundsInLocal().getWidth();
      double titleH = title.getBoundsInLocal().getHeight();

      double subtitleW = subtitle.getBoundsInLocal().getWidth();
      double subtitleH = subtitle.getBoundsInLocal().getHeight();

      title.setX((stage.getWidth() - titleW) / 2);
      title.setY((stage.getHeight() - titleH) / 2);

      subtitle.setX((stage.getWidth() - subtitleW) / 2);
      subtitle.setY(((stage.getHeight() - subtitleH) / 2) + titleH);

      title.setFill(Color.WHITE);
      subtitle.setFill(Color.WHITE);

      root.getChildren().addAll(overlay, title, subtitle);

      System.out.println("Game over: " + !isRunning);
    }
  }

  public void spawnObstacles(Stage stage, Pane root, double numberOfObstacles) {
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

  public void moveObstacles(Pane root, Stage stage) {
    ArrayList<Rectangle> toRemove = new ArrayList<>();

    for (Rectangle ob : obstacles) {
      ob.setX(ob.getX() - 0.5);

      if (ob.getX() < 0 - ob.getWidth()) {
        toRemove.add(ob);
      }
    }

    for (Rectangle ob : toRemove) {
      obstacles.remove(ob);
      root.getChildren().remove(ob);
    }
  }

  private void restart(Pane root, Stage stage, AnimationTimer animationTimer) {
    isRunning = true;
    bird.setTranslateX(50);
    bird.setTranslateY(50);

    velocity[1] = 150;

    // Alte Obstacles entfernen
    root.getChildren().removeAll(obstacles);
    obstacles.clear();
    obstacleOffset = 0;

    // Score zurücksetzen
    score = 0;
    scoreText.setText("Score: " + String.valueOf(score).toCharArray()[0]);

    // Overlays und Texte entfernen
    root.getChildren().removeIf(node ->
            node instanceof Text && node != scoreText ||
                    (node instanceof Rectangle && node != bird));

    // Neue Obstacles spawnen (werden dort bereits zur root hinzugefügt)
    spawnObstacles(stage, root, 5);

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
 
  
  // Anfang Methoden
  
  public static void main(String[] args) {

    launch(args);

  } // end of main
  
  // Ende Methoden
} // end of class FlappyBird
