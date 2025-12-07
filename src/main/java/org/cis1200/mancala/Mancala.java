package org.cis1200.mancala;

/**
 * CIS 120 HW09 - TicTacToe Demo
 * (c) University of Pennsylvania
 * Created by Bayley Tuch, Sabrina Green, and Nicolas Corona in Fall 2020.
 */

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * This class is a model for TicTacToe.
 * 
 * This game adheres to a Model-View-Controller design framework.
 * This framework is very effective for turn-based games. We
 * STRONGLY recommend you review these lecture slides, starting at
 * slide 8, for more details on Model-View-Controller:
 * https://www.seas.upenn.edu/~cis120/current/files/slides/lec36.pdf
 * 
 * This model is completely independent of the view and controller.
 * This is in keeping with the concept of modularity! We can play
 * the whole game from start to finish without ever drawing anything
 * on a screen or instantiating a Java Swing object.
 * 
 * Run this file to see the main method play a game of TicTacToe,
 * visualized with Strings printed to the console.
 */
public class Mancala {

    private int[][] board;
    private int currentPlayer; // 0 = top row, player0      1 = bottom row, player1
    private boolean gameOver;

    private LinkedList<GameState> undoHistory;


    private static final int STORE0_ROW = 0;
    private static final int STORE1_ROW = 1;
    private static final int STORE0_COL = 0;
    private static final int STORE1_COL = 7;
    private static final int NUM_PITS = 6;

    private static class GameState{
        int[][] board;
        int currentPlayer;
        boolean gameOver;

        GameState(int[][] board, int currentPlayer, boolean gameOver) {
            this.board = new int[2][8];
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 8; j++) {
                    this.board[i][j] = board[i][j];
                }
            }
            this.currentPlayer = currentPlayer;
            this.gameOver = gameOver;
        }
    }
    /**
     * Constructor sets up game state.
     */
    public Mancala() {
        undoHistory = new LinkedList<>();
        reset();
    }

    /**
     * reset (re-)sets the game state to start a new game.
     */
    public void reset() {
        board = new int[][] {{0, 4, 4, 4, 4, 4, 4, 0},
                             {0, 4, 4, 4, 4, 4, 4, 0}};
        currentPlayer = 0;
        gameOver = false;
        undoHistory.clear();
    }

    private void saveState() {
        undoHistory.addFirst(new GameState(board,currentPlayer, gameOver));
    }

    public boolean undo() {
        if (undoHistory.isEmpty()) {
            return false;
        }

        // change back to previous game state
        GameState previousState = undoHistory.removeFirst();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = previousState.board[i][j];
            }
        }
        currentPlayer = previousState.currentPlayer;
        gameOver = previousState.gameOver;

        return true;
    }
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int getPitCount (int row, int col) {
        return board[row][col];
    }

    public int getStoreCount(int player) {
        if (player == 0) {
            return board[STORE0_ROW][STORE0_COL];
        } else {
            return board[STORE1_ROW][STORE1_COL];
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private boolean isValidPit(int row, int col) {
        if (gameOver) { return false; }
        // selected pit is store
//        if (row == STORE0_ROWTOP && col == STORE0_COLTOP || row == STORE0_ROWBOT && col == STORE0_COLBOT) { return false; }
//        if (row == STORE1_ROWTOP && col == STORE1_COLTOP || row == STORE1_ROWBOT && col == STORE1_COLBOT) { return false; }
        if (col == STORE0_COL || col == STORE1_COL) {
            return false;
        }

        // selected pit is not on player's side
        if (currentPlayer == 0 && row != STORE0_ROW) {
            return false;
        }
        if (currentPlayer == 1 && row != STORE1_ROW) {
            return false;
        }
        // selected pit has stones
        return board[row][col] > 0;
    }

    /**
     * playTurn allows players to play a turn. Returns true if the move is
     * successful and false if a player tries to play in a location that is
     * taken or after the game has ended. If the turn is successful and the game
     * has not ended, the player is changed. If the turn is unsuccessful or the
     * game has ended, the player is not changed.
     *
     * @param c column to play in
     * @param r row to play in
     * @return whether the turn was successful
     */
    public boolean playTurn(int r, int c) {
        boolean extraTurn = false;

        if (!isValidPit(r, c)) {         // if no stones in selected pit OR game is already over
            return false;
        }

        saveState();

        int[] lastPit = distributeStones(r, c);
        int lastRow = lastPit[0];
        int lastCol = lastPit[1];
        int previousValue = lastPit[2];

        // EXTRA TURN
        // if last stone ends in player's store
        // let player go again
        // repeats playturn
        if (currentPlayer == 0 && lastRow == STORE0_ROW && lastCol == STORE0_COL) {
            extraTurn = true;
        } else if (currentPlayer == 1 && lastRow == STORE1_ROW && lastCol == STORE1_COL) {
            extraTurn = true;
        }

        // CAPTURE RULE
        // if last stone ends in empty pit on player's side & doesn't land on store
        // retrieve the # of stones on opponent's side (diff row, same col)
        // add to player's score + 1
        // set player and opponent's pits to 0
        if (!extraTurn && lastCol != STORE0_COL && lastCol != STORE1_COL) {
            if (previousValue == 0 && board[lastRow][lastCol] == 1 && lastRow == currentPlayer) {
                applyCapture(lastRow, lastCol);
            }
        }


        if (checkWinner() == -1) {
            if (!extraTurn) {
                switchCurrentPlayer();
            }
        }
        return true;
    }

    public int[] distributeStones(int row, int col) {
        int stones = board[row][col];
        int previous = board[row][col];
        board[row][col] = 0;

        // start mvmt on next pit
        if (currentPlayer == 0) {   // row == 0 (top row)
            col--;
        } else {    // row == 1 (bottom row)
            col++;
        }

        while (stones > 0) {
            if (currentPlayer == 0) {
                if (row == STORE0_ROW && col < STORE0_COL) { // wrap to second row
                    row = STORE1_ROW;
                    col = 1;
                }
            } else {
                if (row == STORE1_ROW && col > STORE1_COL) {  // wrap to first row
                    row = STORE0_ROW;
                    col = 6;
                }
            }

            // Skip opponent's store
            if (currentPlayer == 0 && row == STORE1_ROW && col == STORE1_COL) {
                row = STORE0_ROW;
                col = 6;
            }
            if (currentPlayer == 1 && row == STORE0_ROW && col == STORE0_COL) {
                row = STORE1_ROW;
                col = 1;
            }

            // Drop stone
            previous = board[row][col];
            board[row][col]++;
            stones--;


            if (stones > 0) {
                if (currentPlayer == 0) {
                    if (row == STORE0_ROW) {
                        col--;      // Moving left on own side (Row 0)
                    } else { // row == STORE1_ROW
                        col++;      // Moving right on opponent's side (Row 1)
                    }
                } else {    // currentPlayer = 1
                    if (row == STORE1_ROW) {
                        col++;      // Moving right on own side (Row 1)
                    } else { // row == STORE0_ROW
                        col--;      // Moving left on opponent's side (Row 0)
                    }
                }
            }
        }
        return new int[]{row, col, previous};
    }


    /**
     * checkWinner checks whether the game has reached a win condition.
     *
     * @return 0 if nobody has won yet, 1 if player 1 has won, and 2 if player 2
     *         has won, 3 if the game hits stalemate
     */

    private void applyCapture (int row, int col) {
        int oppositeRow;
        int capturedStones;

        // PLAYER Capture
        // Must be on player's side
        if (row != currentPlayer) { return; }

        if (currentPlayer == 0) {
            oppositeRow = 1;
        }
        else {
            oppositeRow = 0;
        }
        capturedStones = board[oppositeRow][col];

        if (capturedStones > 0) {
            if (currentPlayer == 0) {
                board[0][STORE0_COL] += capturedStones + 1;
            } else {
                board[1][STORE1_COL] += capturedStones + 1;
            }
            board[row][col] = 0;
            board[oppositeRow][col] = 0;
        }
    }

    private void switchCurrentPlayer () {
        if (currentPlayer == 0) {
            currentPlayer = 1;
        }
        else {
            currentPlayer = 0;
        }
    }

    public boolean isPlayer0Winner() {
        int count = 0;
        for (int j = 1; j < 8; j++) {
            if (board[0][j] == 0) {
                count++;
            }
        }
        return count == 6;
    }

    public boolean isPlayer1Winner() {
        int count = 0;
        for (int j = 1; j < 8; j++) {
            if (board[1][j] == 0) {
                count++;
            }
        }
        return count == 6;
    }

    private void collectRemainingStones() {
        // collect P0's remaining stones (pits [0][1] to [0][6]) and put them in P0's store
        for (int i = 1; i <= 6; i++) {
            board[0][STORE0_COL] += board[0][i];
            board[0][i] = 0;
        }

        // collect P1's remaining stones (pits [1][0] to [1][5]) and put them in P1's store
        for (int i = 1; i <= 6; i++) {
            board[1][STORE1_COL] += board[1][i];
            board[1][i] = 0;
        }
    }

    public int checkWinner() {
        if (isPlayer0Winner() || isPlayer1Winner()) {
            // Game is over, collect remaining stones.
            collectRemainingStones();
            gameOver = true;

            int p0_score = board[0][STORE0_COL];
            int p1_score = board[1][STORE1_COL];

            if (p0_score > p1_score) {
                return 0; // Player 0 wins
            } else if (p1_score > p0_score) {
                return 1; // Player 1 wins
            } else {
                return 2; // Tie
            }
        }

        // Game continues
        return -1;
    }

    // Serialization - save game state
    public void saveToFile(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        // Save board state
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                writer.write(board[i][j] + " ");
            }
            writer.newLine();
        }

        writer.write(currentPlayer + "");   // save current player
        writer.newLine();

        if (gameOver) {     // save winner
            writer.write("1");
        } else {
            writer.write("0");
        }
        writer.newLine();

        writer.close();
    }

    // Deserialization - load game state
    public void loadFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));

        // Load board state
        for (int i = 0; i < 2; i++) {
            String line = reader.readLine();
            String[] values = line.trim().split(" ");
            for (int j = 0; j < 8; j++) {
                board[i][j] = Integer.parseInt(values[j]);
            }
        }
        currentPlayer = Integer.parseInt(reader.readLine().trim());
        gameOver = reader.readLine().equals("1");

        reader.close();
        undoHistory.clear();
    }

    /**
     * printGameState prints the current game state
     * for debugging.
     */
    public void printGameState(Mancala game) {
        System.out.println("Player0's Score: " + board[0][STORE0_COL]);
        System.out.println("Player1's Score: " + board[1][STORE1_COL]);

        System.out.println("Top Row (Player 0):");
        for (int c = 0; c < 8; c++) {
            System.out.print(game.getPitCount(0, c) + " ");
        }
        System.out.println();

        System.out.println("Bottom Row (Player 1):");
        for (int c = 0; c < 8; c++) {
            System.out.print(game.getPitCount(1, c) + " ");
        }
        System.out.println("\n-----------------------");
    }


    /**
     * getCell is a getter for the contents of the cell specified by the method
     * arguments.
     *
     * @param c column to retrieve
     * @param r row to retrieve
     * @return an integer denoting the contents of the corresponding cell on the
     *         game board. 0 = empty, 1 = Player 1, 2 = Player 2
     */


    /**
     * This main method illustrates how the model is completely independent of
     * the view and controller. We can play the game from start to finish
     * without ever creating a Java Swing object.
     *
     * This is modularity in action, and modularity is the bedrock of the
     * Model-View-Controller design framework.
     *
     * Run this file to see the output of this method in your console.
     */
    public static void main(String[] args) {
        Mancala game = new Mancala();
        game.printGameState(game);

        game.playTurn(0, 4);   // Extra Turn
        game.printGameState(game);

        game.playTurn(0, 1);   // Cross over to opponent's side of board
        game.printGameState(game);

        game.playTurn(1, 2);    // Opponent crosses over to other side of board, as well
        game.printGameState(game);

        game.playTurn(0, 5); // Capture
        game.printGameState(game);


        if (game.isGameOver()) {
            int p0 = game.getStoreCount(0);
            int p1 = game.getStoreCount(1);
            System.out.println("Game Over!");
            System.out.println("Player 0 score: " + p0);
            System.out.println("Player 1 score: " + p1);
        }



    }
}
