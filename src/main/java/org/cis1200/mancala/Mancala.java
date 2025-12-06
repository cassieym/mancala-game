package org.cis1200.mancala;

/**
 * CIS 120 HW09 - TicTacToe Demo
 * (c) University of Pennsylvania
 * Created by Bayley Tuch, Sabrina Green, and Nicolas Corona in Fall 2020.
 */

import java.lang.ref.SoftReference;

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

    private static final int STORE0_ROW = 0;
    private static final int STORE0_COL = 0;
    private static final int STORE1_ROW = 1;
    private static final int STORE1_COL = 6;


    /**
     * Constructor sets up game state.
     */
    public Mancala() {
        reset();
    }

    /**
     * reset (re-)sets the game state to start a new game.
     */
    public void reset() {
        board = new int[][] {{0, 4, 4, 4, 4, 4, 4},
                             {4, 4, 4, 4, 4, 4, 0}};
        currentPlayer = 0;
        gameOver = false;
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
        if (row == STORE0_ROW && col == STORE0_COL) { return false; }
        if (row == STORE1_ROW && col == STORE1_COL) { return false; }

        // selected pit is not on player's side and not own store
        if (currentPlayer == 0) {
            if (row != 0 || col == 0) return false;
        } else { // currentPlayer == 1
            if (row != 1 || col == 6) return false;
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
        int[] lastPit = distributeStones(r, c);
        int lastRow = lastPit[0];
        int lastCol = lastPit[1];
        int previousValue = lastPit[2];

        // EXTRA TURN
        // if last stone ends in player's store
        // let player go again
        // repeats playturn
        if ((lastRow == STORE0_ROW && lastCol == STORE0_COL && currentPlayer == 0)
                || (lastRow == STORE1_ROW && lastCol == STORE1_COL && currentPlayer == 1)) {
            extraTurn = true;
        }

        // CAPTURE RULE
        // if last stone ends in empty pit on player's side & doesnt land on store
        // retrieve the # of stones on opponent's side (diff row, same col)
        // add to player's score + 1
        // set player and opponent's pits to 0
        if ((r != STORE0_ROW && c != STORE0_COL) && (r != STORE1_ROW && c != STORE1_COL)) {
            if (previousValue == 0 && board[r][c] == 1) {
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
                if (row == 0 && col < 0) { // wrap to second row
                    row = 1;
                    col = 0;
                }
            } else {
                if (row == 1 && col > 6) {  // wrap to first row
                    row = 0;
                    col = 6;
                }
            }


            // Skip opponent's store
            // P0 skips P1's store at [1][6]
            if (currentPlayer == 0 && row == STORE1_ROW && col == STORE1_COL) {
                row = 0;
                col = 6;
                continue;
            }
            // P1 skips P0's store at [0][0]
            if (currentPlayer == 1 && row == STORE0_ROW && col == STORE0_COL) {
                row = 1;
                col = 0;
                continue;
            }
            // Drop stone
            previous = board[row][col];
            board[row][col]++;
            stones--;

            if (stones > 0) {
                if (currentPlayer == 0) {
                    if (row == 0) {
                        col--;
                    } else {
                        col++;
                    }
                } else {    // currentPlayer = 1
                    if (row == 1) {
                        col++;
                    } else {
                        col--;
                    }
                }
            }
        }
        return new int[] {row, col, previous};
    }


    /**
     * checkWinner checks whether the game has reached a win condition.
     *
     * @return 0 if nobody has won yet, 1 if player 1 has won, and 2 if player 2
     *         has won, 3 if the game hits stalemate
     */

    private void applyCapture (int row, int col) {
        int oppositeRow;
        int oppositeCol = 6 - col;
        int capturedStones;

        // PLAYER 0 Capture
        if (currentPlayer == 0) {
            // Must be on player 0's side (row 0)
            if (row != 0) { return; }

            oppositeRow = 1;
            capturedStones = board[oppositeRow][oppositeCol];
            if (capturedStones > 0) {
                board[STORE0_ROW][STORE0_COL] += capturedStones + 1;    // add captured + last stone to store
                board[row][col] = 0;                    // reset pit to 0
                board[oppositeRow][oppositeCol] = 0;    // reset opponent's captured pit to 0
            }
        }

        else {
            // Must be on player 1's side (row 1)
            if (row != 1) { return; }

            oppositeRow = 0;
            capturedStones = board[oppositeRow][oppositeCol];
            if (capturedStones > 0) {
                board[STORE1_ROW][STORE1_COL] += capturedStones + 1;    // add captured + last stone to store
                board[row][col] = 0;
                board[oppositeRow][oppositeCol] = 0;
            }
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
        for (int j = 0; j < 6; j++) {
            if (board[0][j] == 0) {
                count++;
            }
        }
        return count == 6;
    }

    public boolean isPlayer1Winner() {
        int count = 0;
        for (int j = 0; j < 6; j++) {
            if (board[1][j] == 0) {
                count++;
            }
        }
        return count == 6;
    }

    private void collectRemainingStones() {
        // collect P0's remaining stones (pits [0][1] to [0][6]) and put them in P0's store
        for (int i = 1; i <= 6; i++) {
            board[STORE0_ROW][STORE0_COL] += board[0][i];
            board[0][i] = 0;
        }

        // collect P1's remaining stones (pits [1][0] to [1][5]) and put them in P1's store
        for (int i = 0; i <= 5; i++) {
            board[STORE1_ROW][STORE1_COL] += board[1][i];
            board[1][i] = 0;
        }
    }

    public int checkWinner() {
        if (isPlayer0Winner() || isPlayer1Winner()) {
            // Game is over, collect remaining stones.
            collectRemainingStones();
            gameOver = true;

            int p0_score = board[STORE0_ROW][STORE0_COL];
            int p1_score = board[STORE1_ROW][STORE1_COL];

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

    /**
     * printGameState prints the current game state
     * for debugging.
     */
    public void printGameState(Mancala game) {
        System.out.println("Player0's Score: " + board[STORE0_ROW][STORE0_COL]);
        System.out.println("Player1's Score: " + board[STORE1_ROW][STORE1_COL]);

        System.out.println("Top Row (Player 0):");
        for (int c = 0; c < 7; c++) {
            System.out.print(game.getPitCount(0, c) + " ");
        }
        System.out.println();

        System.out.println("Bottom Row (Player 1):");
        for (int c = 0; c < 7; c++) {
            System.out.print(game.getPitCount(1, c) + " ");
        }
        System.out.println("\n--------------------");
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

        game.playTurn(0, 6);    // Score: 0
        game.printGameState(game);

        game.playTurn(1, 0);    // Score: 0
        game.printGameState(game);

        game.playTurn(0, 5);
        game.printGameState(game);

        game.playTurn(1, 5);
        game.printGameState(game);

        game.playTurn(0, 4);
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
