package mancala;

/*
 * Mancala
 * (c) University of Pennsylvania
 * Created by Cassie Mai in Fall 2025.
 */

import javax.swing.*;
import java.awt.*;

/** Model-View-Controller framework; Game initializes the view,
 * implements a bit of controller functionality through the reset
 * button, and then instantiates a GameBoard. */
public class RunMancala implements Runnable {
    public void run() {
        // Top-level frame in which game components live
        final JFrame frame = new JFrame("Mancala");
        Rectangle dimensions = frame.getBounds();
        int h = dimensions.height;
        int w = dimensions.width;
        frame.setLocation(400, 300);

        // Status Panel
        final JPanel statusPanel = new JPanel();
        frame.add(statusPanel, BorderLayout.SOUTH);
        final JLabel status = new JLabel("Setting up...");
        statusPanel.add(status);

        // Game Board
        final GameBoard board = new GameBoard(status);
        frame.add(board, BorderLayout.CENTER);

        // Control Panel
        final JPanel controlPanel = new JPanel();
        frame.add(controlPanel, BorderLayout.NORTH);

        // INSTRUCTIONS BUTTON
        final JButton instruc = new JButton("Instructions");
        instruc.addActionListener(e -> board.instructions());
        controlPanel.add(instruc);

        // LOAD BUTTON
        final JButton load = new JButton("Load");
        load.addActionListener(e -> board.load());
        controlPanel.add(load);

        // SAVE BUTTON
        final JButton save = new JButton("Save");
        save.addActionListener(e -> board.save());
        controlPanel.add(save);

        // UNDO BUTTON
        final JButton undo = new JButton("Undo");
        undo.addActionListener(e -> board.undo());
        controlPanel.add(undo);

        // RESET BUTTON
        final JButton reset = new JButton("Reset");
        reset.addActionListener(e -> board.reset());
        controlPanel.add(reset);

        // Put the frame on the screen
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(800, 400));

        // Start game
        board.reset();

    }
}
