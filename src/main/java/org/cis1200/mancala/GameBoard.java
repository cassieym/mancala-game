package org.cis1200.mancala;

/*
 * CIS 120 HW09 - TicTacToe Demo
 * (c) University of Pennsylvania
 * Created by Bayley Tuch, Sabrina Green, and Nicolas Corona in Fall 2020.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

    // Game constants
    public static final int BOARD_WIDTH = 700;
    public static final int BOARD_HEIGHT = 200;

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

        /*
         * Listens for mouseclicks. Updates the model, then updates the game
         * board based off of the updated model.
         */
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Point p = e.getPoint();

                int pitW = BOARD_WIDTH / 7;
                int pitH = BOARD_HEIGHT / 2;

                int col = p.x / pitW;
                int row = p.y / pitH;

                // updates the model given the coordinates of the mouseclick
                game.playTurn(row, col);

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

        // Draws board grid
        int pitW = BOARD_WIDTH / 7;
        int pitH = BOARD_HEIGHT / 2;

        // Draws X's and O's
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 7; c++) {
                int x = c * pitW;
                int y = r * pitH;

                // Pit rectangle
                g.setColor(Color.BLACK);
                g.drawRect(x, y, pitW, pitH);

                // Store pits shaded
                if ((r == 0 && c == 0) || (r == 1 && c == 6)) {
                    g.setColor(new Color(230,230,255));
                    g.fillRect(x+1, y+1, pitW-2, pitH-2);
                }

                // Draw stone count
                g.setColor(Color.BLACK);
                int stones = game.getPitCount(r, c);
                g.drawString(Integer.toString(stones), x + pitW/2, y + pitH/2);
            }
            }
        }

    /**
     * Returns the size of the game board.
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    }
}
