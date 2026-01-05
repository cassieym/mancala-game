package mancala;

/*
 * Mancala
 * (c) University of Pennsylvania
 * Created by Cassie Mai in Fall 2025.
 */

import java.io.*;
import java.util.LinkedList;

/** Model for TicTacToe. */
public class Mancala {

    private int[][] board;
    private int currentPlayer; // 1 = top row, player1     2 = bottom row, player2
    private boolean gameOver;

    private LinkedList<GameState> undoHistory;


    private static final int STORE1_ROW = 0;
    private static final int STORE1_COL = 0;
    private static final int STORE2_ROW = 1;
    private static final int STORE2_COL = 7;

    private static class GameState {
        int[][] board;
        int currentPlayer;
        boolean gameOver;

        GameState(int[][] board, int currentPlayer, boolean gameOver) {
            this.board = new int[2][8];
            for (int i = 0; i < 2; i++) {
                System.arraycopy(board[i], 0, this.board[i], 0, 8);
            }
            this.currentPlayer = currentPlayer;
            this.gameOver = gameOver;
        }
    }
    /** Constructor sets up game state. */
    public Mancala() {
        undoHistory = new LinkedList<>();
        reset();
    }

    /** resets the game state to start a new game. */
    public void reset() {
        board = new int[][] {{0, 4, 4, 4, 4, 4, 4, 0},
                             {0, 4, 4, 4, 4, 4, 4, 0}};
        currentPlayer = 1;
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
            System.arraycopy(previousState.board[i], 0, board[i], 0, 8);
        }
        currentPlayer = previousState.currentPlayer;
        gameOver = previousState.gameOver;

        return true;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int getPitCount(int row, int col) {
        return board[row][col];
    }

    public int getStoreCount(int player) {
        if (player == 1) {
            return board[STORE1_ROW][STORE1_COL];
        } else {
            return board[STORE2_ROW][STORE2_COL];
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private boolean isValidPit(int row, int col) {
        if (gameOver) {
            return false;
        }
        // selected pit is store
        if (col == STORE1_COL || col == STORE2_COL) {
            return false;
        }

        // selected pit is not on player's side
        if (currentPlayer == 1 && row != STORE1_ROW) {
            return false;
        }
        if (currentPlayer == 2 && row != STORE2_ROW) {
            return false;
        }
        // selected pit has stones
        return board[row][col] > 0;
    }

    /**
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

        int[] landingPit = distributeStones(r, c);  // returns the coordinate of pit last stone ends in
        int landingPositionRow = landingPit[0];
        int landingPositionCol = landingPit[1];

        // EXTRA TURN
        // if last stone ends in player's store --> let player go again
        // repeats playturn
        if ((currentPlayer == 1 && landingPositionRow == STORE1_ROW && landingPositionCol == STORE1_COL) ||
            (currentPlayer == 2 && landingPositionRow == STORE2_ROW && landingPositionCol == STORE2_COL)) {
            extraTurn = true;
        }

        // CAPTURE RULE
        // if last stone ends in empty non-store pit on player's side:
        // retrieve the # of stones on opponent's side (diff row, same col)
        // add to player's score + 1
        // set player and opponent's pits to 0
        if (!extraTurn && landingPositionCol != STORE1_COL && landingPositionCol != STORE2_COL) {
            if (board[landingPositionRow][landingPositionCol] == 1 && landingPositionRow == currentPlayer - 1) {
                applyCapture(landingPositionRow, landingPositionCol);
            }
        }

        if (checkWinner() == -1) {  // no winner yet, switch turn to next player
            if (!extraTurn) {
                switchCurrentPlayer();
            }
        }
        return true;
    }

    public int[] distributeStones(int row, int col) {
        // Get stones from the selected pit
        int stones = board[row][col];
        board[row][col] = 0;    // empty current pit

        int currRow = row;
        int currCol = col;
        // Distribute stones
        while (stones > 0) {
            // Start from the next position
            if (currRow == 0) {
                currCol--;
                if (currCol < 0) {  // wrap to bottom row (1,1)
                    currRow = 1;
                    currCol = 1;
                }
            } else { // currRow == 1
                currCol++;
                if (currCol > 7) {  // wrap to top row (0, 6)
                    currRow = 0;
                    currCol = 6;
                }
            }

            // Skip opponent's store (not either store)
            if (!(currentPlayer == 1 && currRow == 1 && currCol == 7)
                    && !(currentPlayer == 2 && currRow == 0 && currCol == 0)) {
                // Place a stone
                board[currRow][currCol]++;
                stones--;

//                // If this was the last stone, done
//                if (stones == 0) {
//                    break;
//                }
//            }

//            // Move to next position
//            if (row == 0) {
//                col--;
//                if (col < 0) {
//                    row = 1;
//                    col = 1;
//                }
//            } else { // row == 1
//                col++;
//                if (col > 7) {
//                    row = 0;
//                    col = 6;
//                }
            }
        }
        return new int[]{currRow, currCol};
    }



    private void applyCapture(int row, int col) {
        // Must be on player's side
        // Player 1: row 0, Player 2: row 1
        if (row != currentPlayer - 1) {
            return;
        }

        int oppositeRow;
        if (currentPlayer == 2) {
            oppositeRow = 0;
        } else {
            oppositeRow = 1;
        }

        int capturedStones = board[oppositeRow][col];

        if (capturedStones > 0) {
            // Add captured stones + the stone we just placed to player's store
            if (currentPlayer == 1) {
                board[STORE1_ROW][STORE1_COL] += capturedStones + 1;
            } else {
                board[STORE2_ROW][STORE2_COL] += capturedStones + 1;
            }

            // Clear both pits
            board[row][col] = 0;
            board[oppositeRow][col] = 0;
        }
    }

    private void switchCurrentPlayer() {
        if (currentPlayer == 1) {
            currentPlayer = 2;
        } else {
            currentPlayer = 1;
        }
    }

    public int checkWinner() {
        boolean p1Empty = true;
        boolean p2Empty = true;

        // Check if P1 pits are empty
        for (int i = 1; i < 7; i++) {
            if (board[0][i] > 0) {
                p1Empty = false;
                break;
            }
        }
        // Check if P2 pits are empty
        for (int i = 1; i < 7; i++) {
            if (board[1][i] > 0) {
                p2Empty = false;
                break;
            }
        }

        if (p1Empty || p2Empty) {
            collectRemainingStones();
            gameOver = true;
            int p1stones = board[STORE1_ROW][STORE1_COL];
            int p2stones = board[STORE2_ROW][STORE2_COL];
            if (p1stones > p2stones) return 1;  // Player 1 wins
            if (p2stones > p1stones) return 2;  // Player 2 wins
            return 3; // Tie
        }
        return -1;   // Game continues
    }

    private void collectRemainingStones() {
        // collect P1's remaining stones (pits [0][1] to [0][6]) and put them in P1's store
        for (int i = 1; i < 7; i++) {
            board[STORE1_ROW][STORE1_COL] += board[0][i];
            board[0][i] = 0;
        }

        // collect P2's remaining stones (pits [1][0] to [1][5]) and put them in P2's store
        for (int i = 1; i < 7; i++) {
            board[STORE2_ROW][STORE2_COL] += board[1][i];
            board[1][i] = 0;
        }
    }

    // Serialization - save game state
    public void saveToFile(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        // save board state
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                writer.write(board[i][j] + " ");
            }
            writer.newLine();
        }

        writer.write(currentPlayer + "");   // save current player
        writer.newLine();

        if (gameOver) {
            writer.write("Over");
        } else {
            writer.write("InProgress");
        }
        writer.newLine();

        writer.close();
    }

    // Deserialization - load game state
    public void loadFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));

        for (int i = 0; i < 2; i++) {
            String line = reader.readLine();
            String[] values = line.trim().split(" ");
            for (int j = 0; j < 8; j++) {
                board[i][j] = Integer.parseInt(values[j]);
            }
        }
        currentPlayer = Integer.parseInt(reader.readLine().trim());
        gameOver = reader.readLine().equals("Over");

        reader.close();
        undoHistory.clear();
    }

    /** prints the current game state */
    public void printGameState(Mancala game) {
        System.out.println("Player1's Score: " + board[STORE1_ROW][STORE1_COL]);
        System.out.println("Player2's Score: " + board[STORE2_ROW][STORE2_COL]);

        System.out.println("Top Row (Player 1):");
        for (int c = 0; c < 8; c++) {
            System.out.print(game.getPitCount(0, c) + " ");
        }
        System.out.println();

        System.out.println("Bottom Row (Player 2):");
        for (int c = 0; c < 8; c++) {
            System.out.print(game.getPitCount(1, c) + " ");
        }
        System.out.println("\n-----------------------");
    }

    public static void main(String[] args) {
        Mancala game = new Mancala();
        game.printGameState(game);

        game.playTurn(0, 4);   // Extra Turn
        game.printGameState(game);

        game.playTurn(0, 1);   // Cross over to opponent's side of board
        game.printGameState(game);

        game.playTurn(1, 3);    // Opponent crosses over to other side of board, as well
        game.printGameState(game);

        game.playTurn(0, 5); // Capture
        game.printGameState(game);


        if (game.isGameOver()) {
            int p1 = game.getStoreCount(0);
            int p2 = game.getStoreCount(1);
            System.out.println("Game Over!");
            System.out.println("Player 1 score: " + p1);
            System.out.println("Player 2 score: " + p2);
        }
    }
}
