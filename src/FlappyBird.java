package src;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird class
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipes
    int pipeX = boardWidth;
    int pipeY = 0; // pipe starts from the top of our screen
    int pipeWidth = 64; // scale by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic
    Bird bird;
    int velocityX = -4; // pipes move to the left speed
    int velocityY = 0; // the bird will have some velocity as well but moves up and down
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop; // we start the loop because we want the game to run infinitely
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;

    Clip pointClip;
    Clip dieClip;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Bird image
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        if (!loadSounds()) {
            System.out.println("Error loading sounds. The game may not have sound effects.");
        }

        // Place pipes timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();

        // Game timer
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    private boolean loadSounds() {
        try {
            // Check if the resource is available
            if (getClass().getResourceAsStream("point.wav") == null) {
                System.out.println("point.wav not found");
            }
            if (getClass().getResourceAsStream("die.wav") == null) {
                System.out.println("die.wav not found");
            }
    
            // Load point sound
            AudioInputStream pointStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream("point.wav"));
            pointClip = AudioSystem.getClip();
            pointClip.open(pointStream);
    
            // Load die sound
            AudioInputStream dieStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream("die.wav"));
            dieClip = AudioSystem.getClip();
            dieClip.open(dieStream);
    
            return true; // Success
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            return false; // Failure
        }
    }
    
    

    public void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // Bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        // Pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Display score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        // Bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // Prevent going off the top

        // Pipes
        for (int i = pipes.size() - 1; i >= 0; i--) { // Iterate backwards to avoid issues
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5; // Increment score
                if (pointClip != null) {
                    pointClip.setFramePosition(0); // Rewind to the beginning
                    pointClip.start(); // Play sound
                }
            }

            // Check for collision
            if (collision(bird, pipe)) {
                gameOver = true;
            }

            // Remove pipes that have gone off the screen
            if (pipe.x + pipe.width < 0) {
                pipes.remove(pipe);
            }
        }

        // Check if bird has fallen off the screen
        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
            if (dieClip != null) {
                dieClip.setFramePosition(0); // Rewind to the beginning
                dieClip.start(); // Play game over sound
            }
        }
    }

    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   // a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x &&   // a's top right corner passes b's top left corner
               a.y < b.y + b.height &&  // a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;    // a's bottom left corner passes b's top left corner
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
            if (gameOver) {
                // Restart the game by resetting the conditions
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
