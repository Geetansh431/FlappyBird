package src;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;


public class FlappyBird extends JPanel implements ActionListener, KeyListener{
    int boardWidth = 360;
    int boardHeight = 640;

    //images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //Bird class
    int birdX = boardWidth/8;
    int birdY = boardWidth/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird{
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    //pipes 
    

    //game logic
    Bird bird;
     // the bird will have some velo as well 
     // but the bird only moves up and down 
    int velocityY =0;

    int gravity = 1;

    Timer gameLoop; // we start the loop because we want the game to run infinitely 

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.blue);
        setFocusable(true);
        addKeyListener(this);

        // load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage(); // we used getImage because we want to extract the imageIcon not the whole image
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        //bird image
        bird = new Bird(birdImg);

        //game timer
        gameLoop = new Timer(1000/60,this); // 1000/16 = 16.166
        gameLoop.start();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
       
        //background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        //bird 
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);
    }


    public void move(){
        //bird 
        velocityY+=gravity;
        bird.y +=velocityY;
        // the bird does not move out of the screen so do the max as 0
        // and the bird stops at the very top
        bird.y = Math.max(bird.y,0); 
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move(); // we move the bird per 60 times
        repaint();
    }

    

    @Override
    public void keyPressed(KeyEvent e) {
        //basically when we press a key it triggers this function

       if (e.getKeyCode()==KeyEvent.VK_SPACE) {
        velocityY = -9;
       }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }


    @Override
    public void keyReleased(KeyEvent e) {
      
    }
}
