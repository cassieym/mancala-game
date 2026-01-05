package mancala;

/*
 * Mancala
 * (c) University of Pennsylvania
 * Created by Cassie Mai in Fall 2025.
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MancalaTest {
    private Mancala game;

    @BeforeEach
    public void setUp() {
        game = new Mancala();
    }

    // SETUP TESTS
    @Test
    public void testInitialGameState() {
        // correct initial state
        assertEquals(1, game.getCurrentPlayer());
        assertFalse(game.isGameOver());

        // 4 stones per pit
        for (int col = 1; col < 7; col++) {
            assertEquals(4, game.getPitCount(0, col));
            assertEquals(4, game.getPitCount(1, col));
        }

        // empty stores
        assertEquals(0, game.getStoreCount(1));
        assertEquals(0, game.getStoreCount(2));
    }

    @Test
    public void testResetGame() {
        // arbitrary moves
        game.playTurn(0, 1);
        game.playTurn(1, 1);

        game.reset();

        // check that game is back to initial state
        assertEquals(1, game.getCurrentPlayer());
        assertFalse(game.isGameOver());
        assertEquals(0, game.getStoreCount(1));
        assertEquals(0, game.getStoreCount(2));
        assertEquals(4, game.getPitCount(0, 1));
        assertEquals(4, game.getPitCount(1, 1));
    }

    // MOVES TESTS
    @Test
    public void testInvalidMoveOnEmptyPit() {
        game.playTurn(0, 1);    // empty pit
        game.playTurn(1, 1);

        assertFalse(game.playTurn(0, 1));   // try to play from empty
    }

    @Test
    public void testInvalidMoveWrongSidePit() {
        assertEquals(1, game.getCurrentPlayer());
        // Player 1 tries to play Player 2's pit (row 1)
        assertFalse(game.playTurn(1, 3));
    }

    @Test
    public void testInvalidMoveOnStore() {
        assertFalse(game.playTurn(0, 0)); // Store 1
        assertFalse(game.playTurn(1, 7)); // Store 2
    }

    @Test
    public void testValidMove() {
        assertTrue(game.playTurn(0, 1)); // play turn
        assertEquals(0, game.getPitCount(0, 1)); // pit now empty
        assertEquals(2, game.getCurrentPlayer()); // switch player
    }

    // STONE DISTRIBUTION TESTS
    @Test
    public void testStoneDistribution() {
        game.playTurn(0, 4);    // (0,4)   4 stones

        assertEquals(0, game.getPitCount(0, 4)); // pit empty
        // distribution
        assertEquals(5, game.getPitCount(0, 3)); //  1st stone
        assertEquals(5, game.getPitCount(0, 2)); //  2nd stone
        assertEquals(5, game.getPitCount(0, 1)); // 3rd stone
        assertEquals(1, game.getStoreCount(1));   // 4th stone
    }

    @Test
    public void testDistributionSkipsOpponentStore() {
        // stone overloading setup
        game.playTurn(0, 5);
        game.playTurn(1, 6);
        game.playTurn(0, 4);
        game.playTurn(1, 5);
        game.playTurn(0, 6); // extra turn
        game.playTurn(0, 5);
        game.playTurn(1, 4);
        game.playTurn(0, 4);
        game.playTurn(1, 6);

        assertEquals(2, game.getPitCount(0, 6)); // check that it has 2 as of now
        // should overload across all p1's pits and last stone back to (0,6)
        game.playTurn(0, 2);
        assertEquals(3, game.getPitCount(0, 6));    // should now have 3 stones
    }

    @Test
    public void testBoundaryPits() {
        assertTrue(game.playTurn(0, 1)); // First pit
        assertTrue(game.playTurn(1, 6)); // Last pit
    }

    // EXTRA TURN TESTS
    @Test
    public void testExtraTurnWhenLandingInOwnStore() {
        game.playTurn(0, 4); // last stone ends in store, should grant extra turn
        assertEquals(1, game.getCurrentPlayer());
        assertEquals(1, game.getStoreCount(1));
    }

    @Test
    public void testNoExtraTurnWhenNotLandingInStore() {
        game.playTurn(0, 1);
        assertEquals(2, game.getCurrentPlayer()); // switch to P2
    }

    // CAPTURE TESTS
    @Test
    public void testCaptureOppositeStones() {
        // Capture setup
        game.playTurn(0, 4);   // Extra Turn
        game.playTurn(0, 1);   // empty pit at position (0, 1)
        game.playTurn(1, 3);    // Opponent crosses over to other side of board, as well
        game.playTurn(0, 5); // Capture

        // Check if capture occurred correctly
        assertEquals(0, game.getPitCount(0, 1));    // capturing pit
        assertEquals(0, game.getPitCount(1, 1));    // opposite pit
        assertEquals(8, game.getStoreCount(1));   // store updated w/ new stones
    }

    @Test
    public void testNoCaptureWhenLandingInNonEmptyPit() {
        game.playTurn(0, 4);   // Extra Turn
        game.playTurn(0, 2);   // empty pit at position (0, 2)
        game.playTurn(1, 3);    // Opponent crosses over to other side of board, as well

        assertEquals(2, game.getStoreCount(1));  // store 2
        game.playTurn(0, 5); // shouldnt result in capture
        assertEquals(2, game.getStoreCount(1));  // store unchanged
    }

    // GAME OVER TEST
    @Test
    public void testGameEndOneSideEmpty() {
        for (int i = 0; i < 100; i++) {
            if (game.isGameOver()) {
                break;
            }
            // keep playing random moves
            for (int col = 1; col <= 6; col++) {
                if (game.getPitCount(game.getCurrentPlayer() - 1, col) > 0) {
                    game.playTurn(game.getCurrentPlayer() - 1, col);
                }
            }
        }
        assertTrue(game.isGameOver()); // terminates
    }

    // UNDO TEST
    @Test
    public void testUndoMove() {
        int initialPlayer = game.getCurrentPlayer();
        int initialStones = game.getPitCount(0, 1);
        int initialStonesNeighbor = game.getPitCount(1, 1);
        game.playTurn(0, 1);

        int initialPlayer2 = game.getCurrentPlayer();
        int initialStones2 = game.getPitCount(1, 3);
        int initialStonesNeighbor2 = game.getPitCount(1, 5);
        game.playTurn(1, 3);

        assertTrue(game.undo());
        assertEquals(initialPlayer2, game.getCurrentPlayer());
        assertEquals(initialStones2, game.getPitCount(1, 3));
        assertEquals(initialStonesNeighbor2, game.getPitCount(1, 5));

        assertTrue(game.undo());
        assertEquals(initialPlayer, game.getCurrentPlayer());
        assertEquals(initialStones, game.getPitCount(0, 1));
        assertEquals(initialStonesNeighbor, game.getPitCount(1, 1));
    }

    @Test
    public void testUndoNoHistory() {
        // undo at start of game
        assertFalse(game.undo());
    }
}
