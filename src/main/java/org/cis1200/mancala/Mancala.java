package org.cis1200.mancala;

/**
 * CIS 120 HW09 - TicTacToe Demo
 * (c) University of Pennsylvania
 * Created by Bayley Tuch, Sabrina Green, and Nicolas Corona in Fall 2020.
 */

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

    private int[][] board = {{0, 4, 4, 4, 4, 4, 4}, {4, 4, 4, 4, 4, 4, 0}};
    private int currentPlayer = 1;
    private static final int STORE0_ROW = 0;
    private static final int STORE0_COL = 0;
    private static final int STORE1_ROW = 1;
    private static final int STORE1_COL = 6;
    private boolean gameOver = false;
    private int position;


    /**
     * Constructor sets up game state.
     */
    public Mancala() {
        reset();
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

        if (!isValidPit(r, c)) {         // if no stones in selected pit OR game is already over
            return false;
        }


        int[] lastPit = distributeStones(r, c);
        int lastRow = lastPit[0];
        int lastCol = lastPit[1];

        // EXTRA TURN
        // if last stone ends in player's store
        // let player go again
        // repeats playturn
        if ((lastRow == 0 && lastCol == 0 && currentPlayer == 0) || (lastRow == 1 && lastCol == 6 && currentPlayer == 2)) {
            return true;
        }



        // CAPTURE RULE
        // if last stone ends in empty pit on player's side,
        // retrieve the # of stones on opponent's side (diff row, same col)
        // add to player's score + 1
        // set player and opponent's pits to 0
        if (currentPlayer == 0 && lastRow == 0){
            if (board[lastRow][lastCol] == 1) {
                applyCapture(lastRow, lastCol);
            }
        }

        switchCurrentPlayer(currentPlayer);

        checkWinner();
        return gameOver;
    }

    public int[] distributeStones(int row, int col) {
        int stones = board[row][col];

        while (stones > 0) {

            // PLAYER 0
            if (currentPlayer == 0) {
                 if (row == 0) {     // top of board
                        if (col < 6) {
                            col--;      // move left;
                        } else if (col == 0) {  // at p1's store pit
                            col = 6;
                        } else if (col == 6) {     // move to p2's side of board
                            row = 1;
                            col = 0;
                        }
                } else {    // row == 1 (bottom row)
                    if (col < 5) {
                        col++;      // move right;
                    } else if (col == 5) {  // next pit is store
                        row = 0;
                    }
                }
            // PLAYER 1
            } else {
                if (row == 1) {     // bottom of board
                    if (col < 5) {
                        col++;          // move right;
                    } else if (col == 5) {      // at p2's store pit
                        col = 6;
                    } else if (col == 6) {     // move to p2's side of board
                        row = 0;
                        col = 5;
                    }
                } else {    // row == 0 (top row)
                    if (col > 0) {
                        col--;      // move left;
                    } else if (col == 0) {  // next pit is store
                        row = 1;
                    }
                }
            }
            // Drop stone
            board[row][col]++;
            stones--;
        }
        return new int[] {row, col};
    }


    /**
     * checkWinner checks whether the game has reached a win condition.
     *
     * @return 0 if nobody has won yet, 1 if player 1 has won, and 2 if player 2
     *         has won, 3 if the game hits stalemate
     */
    private void applyCapture(int row, int col) {
        int oppositeRow;
        int capturedStones;

        // PLAYER 0
        if (currentPlayer == 0) {
            oppositeRow = 2;
            capturedStones = board[oppositeRow][col];
            if (capturedStones > 0) {
                board[STORE0_ROW][STORE0_COL] += capturedStones + 1;    // add to store
                board[row][col] = 0;
                board[oppositeRow][col] = 0;
            }
        }

        // PLAYER 1
        else {
            oppositeRow = 1;
            capturedStones = board[oppositeRow][col];
            if (capturedStones > 0) {
                board[STORE1_ROW][STORE1_COL] += capturedStones + 1;    // add to store
                board[row][col] = 0;
                board[oppositeRow][col] = 0;
            }
        }
    }

    private void switchCurrentPlayer(int currentPlayer) {
        if (currentPlayer == 1) {
            currentPlayer = 0;
        }
        else {
            currentPlayer = 1;
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
            if (board[1][j] != 0) {
                count++;
            }
        }
        return count == 6;
    }

    private void collectRemainingStones(int winner) {
        if (winner == 1) {  // WINNER: PLAYER 1
            for (int i = 1; i < 7; i++) {
                board[STORE1_ROW][STORE1_COL] += board[0][i];
                board[0][i] = 0;
            }
        }
        else { // WINNER: PLAYER 0
            for (int i = 0; i < 6; i++) {
                board[STORE0_ROW][STORE0_COL] += board[1][i];
                board[1][i] = 0;
            }
        }
    }
    public int checkWinner() {
        if (isPlayer0Winner() || isPlayer1Winner()) {
            gameOver = true;
            if (board[STORE0_ROW][STORE0_COL] > board[STORE1_ROW][STORE1_COL]) {
                collectRemainingStones(1);
                return 1;
            } else {
                collectRemainingStones(2);
                return 2;
            }
        }

        gameOver = false;
        return 0;
    }

    /**
     * printGameState prints the current game state
     * for debugging.
     */
    public void printGameState() {
        System.out.println("Player1's Score: " + player1Score);
        System.out.println("Player2's Score: " + player2Score);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 7; j++) {
                System.out.print(board[i][j]);
                if (j < 2) {
                    System.out.print(" | ");
                }
            }
            if (i < 2) {
                System.out.println("\n---------");
            }
        }
    }

    /**
     * reset (re-)sets the game state to start a new game.
     */
    public void reset() {
        board = new int[][] {{0, 4, 4, 4, 4, 4, 4}, {4, 4, 4, 4, 4, 4, 0}};
        currentPlayer = 1;
        gameOver = false;
    }

    /**
     * getCurrentPlayer is a getter for the player
     * whose turn it is in the game.
     * 
     * @return true if it's Player 1's turn,
     *         false if it's Player 2's turn.
     */
    public int getCurrentPlayer() {
        return this.currentPlayer;
    }

    public int getStones (int row, int col) {
        return board[row][col];
    }

    public int getStoreCount (int player) {
        if (player == 1) {
            return board[0][0];
        }
        else {
            return board[1][6];
        }
    }

    private boolean isValidPit(int row, int col) {
        if (gameOver) { return false; }
        // selected pit is store
        if (col == 0 && row == 0) { return false; }
        if (col == 6 && row == 1) { return false; }

        // selected pit is not on player's side
        if (currentPlayer == 1 && row != 0) return false;
        if (currentPlayer == 2 && row != 1) return false;

        // selected pit has no stones
        return board[row][col] > 0;
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
    public int getPitCount(int c, int r) {
        return board[r][c];
    }




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
        Mancala t = new Mancala();

        t.playTurn(1, 1);
        t.printGameState();

        t.playTurn(0, 0);
        t.printGameState();

        t.playTurn(0, 2);
        t.printGameState();

        t.playTurn(2, 0);
        t.printGameState();

        t.playTurn(1, 0);
        t.printGameState();

        t.playTurn(1, 2);
        t.printGameState();

        t.playTurn(0, 1);
        t.printGameState();

        t.playTurn(2, 2);
        t.printGameState();

        t.playTurn(2, 1);
        t.printGameState();
        System.out.println();
        System.out.println();
        System.out.println("Winner is: " + t.checkWinner());
    }
}
