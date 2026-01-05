package mancala;

/*
 * Mancala
 * (c) University of Pennsylvania
 * Created by Cassie Mai in Fall 2025.
 */

import javax.swing.*;

public class Game {
    public static void main(String[] args) {
        Runnable game = new RunMancala();
        SwingUtilities.invokeLater(game);
    }
}
