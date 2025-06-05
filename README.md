# Flappy Bird – JavaFX Edition

A modern Flappy Bird clone built using JavaFX, featuring smooth animation, responsive controls, and clean, maintainable code.

![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-17.0.6-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)
![Status](https://img.shields.io/badge/Status-Complete-brightgreen.svg)

## 🎮 Gameplay

Fly through randomly placed pipes by pressing `SPACE` to jump. Score a point for each successful pass. One hit = Game Over.

**Controls:**

* `SPACE` – Jump
* `Any Key` – Restart after Game Over

---

## 🔧 Requirements

* **Java 17+**
* **JavaFX SDK** (e.g., JavaFX 17.0.6)

👉 [Download Java](https://openjdk.org/)
👉 [Download JavaFX](https://gluonhq.com/products/javafx/)

---

## 🚀 Getting Started

### 1. Clone

```bash
git clone https://github.com/yourusername/flappy-bird-javafx.git
cd flappy-bird-javafx
```

### 2. Assets Folder Structure

```
src/
├── FlappyBird.java
└── assets/
    ├── textures/
    │   ├── background-day.png
    │   ├── pipe-green.png
    │   ├── yellowbird-*.png
    │   └── gameover.png
    └── fonts/
        └── flappy_bird.ttf
```

### 3. Run the Game

#### CLI:

```bash
javac --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml FlappyBird.java
java --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml FlappyBird
```

#### IntelliJ:

* Add JavaFX SDK under Project Structure → Libraries
* Add VM options:
  `--module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml`

---

## ✨ Features

* 🎯 Smooth 60 FPS gameplay
* 🔁 Procedural pipe generation
* 💥 Accurate collision detection
* 📊 Real-time scoring
* 🧠 Clean OOP design
* 🎨 Custom graphics & font loading
* ⚙️ Error handling & compatibility hints

---

## 🛠 Project Structure

```
flappy-bird-javafx/
├── FlappyBird.java       # Main class
├── assets/               # Images and fonts
└── docs/                 # Optional documentation
```

---

## 💡 Architecture Highlights

* Game loop via `AnimationTimer`
* Gravity & physics-based movement
* Collision via bounding box checks
* Separated game states (Menu / Game / Game Over)
* Refactored for readability and reusability

Example physics snippet:

```java
birdVelocity += GRAVITY * deltaTime;
bird.setTranslateY(bird.getTranslateY() + birdVelocity * deltaTime);
```

---

## 🐞 Common Errors

**❗ JavaFX not found?**
Add the correct `--module-path` and `--add-modules` in your run config.

**❗ NullPointerException on assets?**
Make sure paths are correct and files exist. Use `getClass().getResourceAsStream(...)`.

---

