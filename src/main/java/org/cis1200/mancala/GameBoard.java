package org.cis1200.mancala;

/*
 * CIS 120 HW09 - TicTacToe Demo
 * (c) University of Pennsylvania
 * Created by Bayley Tuch, Sabrina Green, and Nicolas Corona in Fall 2020.
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

/**
 * This class instantiates a TicTacToe object, which is the model for the game.
 * As the user clicks the game board, the model is updated. Whenever the model
 * is updated, the game board repaints itself and updates its status JLabel to
 * reflect the current state of the model.
 * 
 * This game adheres to a Model-View-Controller design framework. This
 * framework is very effective for turn-based games. We STRONGLY
 * recommend you review these lecture slides, starting at slide 8,
 * for more details on Model-View-Controller:
 * https://www.seas.upenn.edu/~cis120/current/files/slides/lec37.pdf
 * 
 * In a Model-View-Controller framework, GameBoard stores the model as a field
 * and acts as both the controller (with a MouseListener) and the view (with
 * its paintComponent method and the status JLabel).
 */
@SuppressWarnings("serial")
public class GameBoard extends JPanel {

    private Mancala game; // model for the game
    private JLabel status; // current status text
    private Image stoneImage;

    // Game constants
//    public static final int BOARD_WIDTH = 700;
//    public static final int BOARD_HEIGHT = 200;

    private static final String SAVE_FILE = "files/mancala_save.txt";

    /**
     * Initializes the game board.
     */
    public GameBoard(JLabel statusInit) {
        // creates border around the court area, JComponent method
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Enable keyboard focus on the court area. When this component has the
        // keyboard focus, key events are handled by its key listener.
        setFocusable(true);

        game = new Mancala(); // initializes model for the game
        status = statusInit; // initializes the status JLabel


        // Load the stone image
        try {
            URL imageUrl = new URL("https://png.pngtree.com/png-vector/20231211/ourmid/pngtree-cartoon-marbles-in-different-colors-for-gamesui-character-png-image_10878052.png"); // Replace with your URL
            stoneImage = ImageIO.read(imageUrl);
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
            stoneImage = null;
        }

        /*
         * Listens for mouseclicks. Updates the model, then updates the game
         * board based off of the updated model.
         */
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Point p = e.getPoint();

                int width = getWidth();
                int height = getHeight();
                int storeWidth = width / 8;
                int pitWidth = (width - 2 * storeWidth) / 6;    // 6 pits
                int rowHeight = height / 2;

                // check if click is in a store (ignore)
                if (p.x < storeWidth || p.x > width - storeWidth) {
                    return;
                }

                // which pit clicked
                int col = (p.x - storeWidth) / pitWidth;
                int row = p.y / rowHeight;

                int boardCol;
                if (row == 0) {
                    boardCol = 6 - col;
                } else {
                    boardCol = col + 1;
                }

                if (boardCol < 1 || boardCol > 6)
                    return;

                // updates the model given the coordinates of the mouseclick
                game.playTurn(row, boardCol);

                updateStatus(); // updates the status JLabel
                repaint(); // repaints the game board
            }
        });
    }

    /**
     * (Re-)sets the game to its initial state.
     */
    public void reset() {
        game.reset();
        status.setText("Player 0's Turn");
        repaint();

        // Makes sure this component has keyboard/mouse focus
        requestFocusInWindow();
    }

    public void undo() {
        if (game.undo()) {
            updateStatus();
            repaint();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No moves to undo",
                    "Undo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void save() {
        try {
            game.saveToFile(SAVE_FILE);
            JOptionPane.showMessageDialog(this,
                    "Game saved successfully!",
                    "Save Game",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving game: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void load() {
        try {
            game.loadFromFile(SAVE_FILE);
            updateStatus();
            repaint();
            JOptionPane.showMessageDialog(this,
                    "Game loaded successfully!",
                    "Load Game",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading game: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates the JLabel to reflect the current state of the game.
     */
    private void updateStatus() {
        status.setText("Player " + game.getCurrentPlayer() + "'s Turn");

        if (game.isGameOver()) {
            int p0 = game.getStoreCount(0);
            int p1 = game.getStoreCount(1);

            if (p0 > p1) status.setText("Player 0 wins!");
            else if (p1 > p0) status.setText("Player 1 wins!");
            else status.setText("Tie game!");
        }
    }

    /**
     * Draws the game board.
     * 
     * There are many ways to draw a game board. This approach
     * will not be sufficient for most games, because it is not
     * modular. All of the logic for drawing the game board is
     * in this method, and it does not take advantage of helper
     * methods. Consider breaking up your paintComponent logic
     * into multiple methods or classes, like Mushroom of Doom.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        int storeWidth = width / 8;
        int pitWidth = (width - 2 * storeWidth) / 6;
        int rowHeight = height / 2;

        // Draw Player 0's store (left side)
        drawStore(g, 0, 0, storeWidth, height, game.getStoreCount(0), "P0");

        // Draw Player 1's store (right side)
        drawStore(g, width - storeWidth, 0, storeWidth, height, game.getStoreCount(1), "P1");


        // Draw pits
        // Top row (Player 0): pits go right to left (columns 6,5,4,3,2,1)
        for (int i = 0; i < 6; i++) {
            int x = storeWidth + (i * pitWidth);
            int y = 0;
            drawPit(g, x, y, pitWidth, rowHeight, game.getPitCount(0, 6-i));
        }

        // Bottom row (Player 1): pits go left to right (columns 0,1,2,3,4,5)
        for (int i = 0; i < 6; i++) {
            int x = storeWidth + (i * pitWidth);
            int y = rowHeight;
            drawPit(g, x, y, pitWidth, rowHeight, game.getPitCount(1, i));
        }
    }
    private void drawStore(Graphics g, int x, int y, int w, int h, int stones, String label) {
        // Draw border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        // Fill with color
        g.setColor(new Color(230, 230, 255));
        g.fillRect(x + 1, y + 1, w - 2, h - 2);

        // Draw label
        g.setColor(Color.BLACK);
        g.drawString(label, x + w / 2 - 10, y + 20);

        // Draw stone count
        g.drawString(String.valueOf(stones), x + w / 2 - 5, y + h / 2);

        // Draw stones
        drawStones(g, x, y, w, h, stones);
    }

    private void drawPit(Graphics g, int x, int y, int w, int h, int stones) {
        // Draw border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        // Draw stone count
        g.drawString(String.valueOf(stones), x + w / 2 - 5, y + h / 2);

        // Draw stones
        drawStones(g, x, y, w, h, stones);
    }

    private void drawStones(Graphics g, int x, int y, int w, int h, int stones) {
        if (stoneImage != null && stones > 0) {
            int imgSize = 15;
            int padding = 5;
            int stonesPerRow = Math.max(1, w / (imgSize + padding));

            // Calculate dimensions of the stone grid
            int rows = (stones + stonesPerRow - 1) / stonesPerRow;
            int lastRowStones = stones % stonesPerRow;
            if (lastRowStones == 0) lastRowStones = stonesPerRow;

            int gridWidth = Math.min(stones, stonesPerRow) * (imgSize + padding) - padding;
            int gridHeight = rows * (imgSize + padding) - padding;

            // Center the grid in the pit
            int startX = x + (w - gridWidth) / 2;
            int startY = y + (h - gridHeight) / 2;

            // Draw multiple stone images
            for (int i = 0; i < stones; i++) {
                int col = i % stonesPerRow;
                int row = i / stonesPerRow;

                // Center the last row if it has fewer stones
                int offsetX = 0;
                if (row == rows - 1 && lastRowStones < stonesPerRow) {
                    offsetX = (stonesPerRow - lastRowStones) * (imgSize + padding) / 2;
                }

                int imgX = startX + offsetX + (col * (imgSize + padding));
                int imgY = startY + (row * (imgSize + padding));
                g.drawImage(stoneImage, imgX, imgY, imgSize, imgSize, null);
            }
        }
    }

    /**
     * Returns the size of the game board.
     */
    @Override
    public Dimension getPreferredSize () {
        return new Dimension(getWidth(), getHeight());
    }
}

