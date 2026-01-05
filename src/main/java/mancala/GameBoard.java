package mancala;

/*
 * Mancala
 * (c) University of Pennsylvania
 * Created by Cassie Mai in Fall 2025.
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

/**
 * Model-View-Controller framework; GameBoard stores the model as a field
 * and acts as both the controller (with a MouseListener) and the view (with
 * its paintComponent method and the status JLabel).
 */
public class GameBoard extends JPanel {

    private Mancala game; // model for the game
    private JLabel status; // current status text
    private Image stoneImage;

    private static final String SAVE_FILE = "files/mancala_save.txt";

    /** Initializes the game board */
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
            URL imageUrl = new URL(
                    "https://png.pngtree.com/png-vector/20231211/ourmid/" +
                            "pngtree-cartoon-marbles-in-different-colors-for" +
                            "-gamesui-character-png-image_10878052.png");
            stoneImage = ImageIO.read(imageUrl);
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
            stoneImage = null;
        }

        /** Listens for mouseclicks. Updates the model, then updates the game
         * board based off of the updated model. */
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
                int col = 1 + (p.x - storeWidth) / pitWidth;
                int row = p.y / rowHeight;

                if (col < 1 || col > 6) {
                    return;
                }

                // updates the model given the coordinates of the mouseclick
                game.playTurn(row, col);

                updateStatus(); // updates the status JLabel
                repaint(); // repaints the game board
            }
        });
    }

    /** Resets the game to its initial state. */
    public void reset() {
        game.reset();
        status.setText("Player 1's Turn");
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

    public void instructions() {
        String instructions =
            "MANCALA GAME RULES \n" +
            "GOAL: " +
            "Collect more stones in your store than your opponent.\n\n" +

            "INFO:\n" +
            "• The vertical rectangles on the left and right " +
                    "outer sides of the board are called 'stores' \n" +
            "• Each player has 6 pits and 1 store \n" +
            "• Player 1's store is on the left \n" +
            "• Player 1's pits are on the top row \n" +
            "• Player 2's store is on the right \n" +
            "• Player 2's pits are on the bottom row \n" +
            "• Each pit starts with 4 stones\n\n" +

            "BASIC RULES:\n" +
            "1. Click on one of your pits (must contain stones)\n" +
            "2. Stones are distributed counter-clockwise, one per pit\n" +
            "3. You place stones in your own store and skip opponent's store\n" +
            "4. The game ends when one player's pits are all empty. \n" +
                "All of the remaining stones go the other player's store \n" +
            "5. The player with the most stones in their store wins \n\n" +

            "SPECIAL RULES:\n" +
            "• EXTRA TURN: If the last stone lands in your store, you get another turn\n" +
            "• CAPTURE: If the last stone lands in an empty pit on your side,\n" +
            "  you capture that stone AND all opponent's stones in the opposite pit\n" +
            "  All captured stones go to your store.\n\n" +

            "BUTTONS:\n" +
            "• Reset: Start a new game\n" +
            "• Undo: Go back to your last move\n" +
            "• Save: Save current game state\n" +
            "• Load: Load previously saved game \n\n" +

            "GOOD LUCK!";


        JOptionPane.showMessageDialog(this,
                instructions,
                "How to Play Mancala",
                JOptionPane.INFORMATION_MESSAGE);
    }


    /** Updates the JLabel to reflect the current state of the game. */
    private void updateStatus() {
        status.setText("Player " + game.getCurrentPlayer() + "'s Turn");

        int gameStatus = game.checkWinner();
        if (game.isGameOver()) {
            if (gameStatus == 1) {
                status.setText("Player 1 wins!");
            } else if (gameStatus == 2) {
                status.setText("Player 2 wins!");
            } else {
                status.setText("Tie game!");
            }
        }
    }


    /** Draws game board. */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();
        int storeWidth = width / 8;
        int pitWidth = (width - 2 * storeWidth) / 6;
        int rowHeight = height / 2;

        // Draw P1's store (left)
        drawStore(g, 0, 0, storeWidth, height, game.getStoreCount(1), "Player 1:");

        // Draw P2's store (right)
        drawStore(g, width - storeWidth, 0, storeWidth, height, game.getStoreCount(2), "Player 2:");


        // Draw pits
        // Top row (Player 1):
        for (int i = 0; i < 6; i++) {
            int x = storeWidth + (i * pitWidth);
            int y = 0;
            drawPit(g, x, y, pitWidth, rowHeight, game.getPitCount(0, i + 1));
        }

        // Bottom row (Player 2):
        for (int i = 0; i < 6; i++) {
            int x = storeWidth + (i * pitWidth);
            int y = rowHeight;
            drawPit(g, x, y, pitWidth, rowHeight, game.getPitCount(1, i + 1));
        }
    }
    private void drawStore(Graphics g, int x, int y, int w, int h, int stones, String label) {
        // Border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        // Fill
        g.setColor(new Color(230, 230, 255));
        g.fillRect(x + 1, y + 1, w - 2, h - 2);

        // P1/P2 Label
        g.setColor(Color.BLACK);
        g.drawString(label, x + w / 2 - 25, y + 20);

        // Stone count label
        g.drawString(String.valueOf(stones), x + w / 2 - 5, y + h / 2);

        drawStones(g, x, y, w, h, stones);
    }

    private void drawPit(Graphics g, int x, int y, int w, int h, int stones) {
        // Border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);

        // Stone count label
        g.drawString(String.valueOf(stones), x + w / 2 - 5, y + h / 2);

        drawStones(g, x, y, w, h, stones);
    }

    private void drawStones(Graphics g, int x, int y, int w, int h, int stones) {
        if (stoneImage != null && stones > 0) {
            int imgSize = 15;
            int padding = 5;
            int stonesPerRow = Math.max(1, w / (imgSize + padding));

            // dimensions of stone grid
            int rows = (stones + stonesPerRow - 1) / stonesPerRow;
            int lastRowStones = stones % stonesPerRow;
            if (lastRowStones == 0) {
                lastRowStones = stonesPerRow;
            }

            int gridWidth = Math.min(stones, stonesPerRow) * (imgSize + padding) - padding;
            int gridHeight = rows * (imgSize + padding) - padding;

            // center grid in the pit
            int startX = x + (w - gridWidth) / 2;
            int startY = y + (h - gridHeight) / 2;

            // draw multiple stone images
            for (int i = 0; i < stones; i++) {
                int col = i % stonesPerRow;
                int row = i / stonesPerRow;

                // center last row
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

    /** Returns the size of the game board. */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }

}

