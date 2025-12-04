package org.cis1200.mushroom;

import org.junit.jupiter.api.*;
import java.awt.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * You can use this file (and others) to test your
 * implementation.
 */

public class MushroomTest {
    @Test
    public void createSquare() {
        Square square = new Square(200, 200, Color.white);

        // square should start at (0, 0)
        assertEquals(0, square.getPx());
        assertEquals(0, square.getPy());

        // square should not be moving at the start
        assertEquals(0, square.getVx());
        assertEquals(0, square.getVy());
    }

    @Test
    public void squareVelocityUpdatesPosition() {
        Square square = new Square(200, 200, Color.white);

        // update velocity to non-zero value
        square.setVx(10);
        square.setVy(20);
        assertEquals(10, square.getVx());
        assertEquals(20, square.getVy());

        // position should not have updated yet since we didn't call move()
        assertEquals(0, square.getPx());
        assertEquals(0, square.getPy());

        // move!
        square.move();

        // square should've moved
        assertEquals(10, square.getPx());
        assertEquals(20, square.getPy());
    }

    @Test
    public void twoObjectIntersection() {
        // square should spawn at (0, 0)
        Square square = new Square(200, 200, Color.white);
        assertEquals(0, square.getPx());
        assertEquals(0, square.getPy());

        // mushroom should spawn at (130, 130)
        Poison mushroom = new Poison(200, 200);
        assertEquals(130, mushroom.getPx());
        assertEquals(130, mushroom.getPy());

        // they're very far apart, so they should not be intersecting
        assertFalse(square.intersects(mushroom));

        // move square on top of mushroom
        square.setPx(130);
        square.setPy(130);

        assertEquals(130, square.getPx());
        assertEquals(130, square.getPy());

        // now, they're on top of one another! they should intersect
        assertTrue(square.intersects(mushroom));
    }
}
