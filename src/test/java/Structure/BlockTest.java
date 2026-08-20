package Structure;

import Ship.Ship;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BlockTest {

    @Test
    void TP01_constructor_sets_default_state() {
        Block b = new Block(2, 3);

        assertEquals(2, b.x);
        assertEquals(3, b.y);
        assertEquals(".", b.show);
        assertFalse(b.placed);
        assertFalse(b.shot);
        assertNull(b.ship);
    }

    @Test
    void TP02_create_resets_state() {
        Block b = new Block(0, 0);

        // change state
        b.show = "X";
        b.placed = true;
        b.shot = true;

        b.create();

        assertEquals(".", b.show);
        assertFalse(b.placed);
        assertFalse(b.shot);
    }

    @Test
    void TP03_place_sets_ship_and_marks_block() {
        Block b = new Block(1, 1);
        Ship s = new Ship(2, 0, 0, "r");

        b.place(s);

        assertTrue(b.placed);
        assertEquals("O", b.show);
        assertSame(s, b.ship);
    }

    @Test
    void TP04_shot_hit_true_sets_X_even_if_not_placed() {
        Block b = new Block(0, 0);

        b.shot(true);

        assertTrue(b.shot);
        assertEquals("X", b.show);
        assertFalse(b.placed); // still not placed
    }

    @Test
    void TP05_shot_hit_false_sets_plus_even_if_not_placed() {
        Block b = new Block(0, 0);

        b.shot(false);

        assertTrue(b.shot);
        assertEquals("+", b.show);
    }

    @Test
    void TP06_shot_on_placed_block_calls_ship_shot() {
        Block b = new Block(0, 0);
        Ship s = new Ship(1, 0, 0, "r");

        b.place(s);       // placed = true, show = O
        b.shot(true);

        assertTrue(s.destroyed); // proof that ship.shot() was called
        assertEquals("X", b.show);
        assertTrue(b.shot);
    }
}
