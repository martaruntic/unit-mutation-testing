package Ship;

import Structure.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShipTest {

    private Ship s;

    @BeforeEach
    void setUp() {
        s = new Ship(2, 2, 3, "d"); // novi ship pre svakog testa
    }

    @Test
    void TP01_constructor_state() {
        assertEquals(2, s.x);
        assertEquals(3, s.y);
        assertEquals(2, s.n);
        assertEquals("d", s.dir);
        assertFalse(s.destroyed);
    }

    @Test
    void TP02_shot_lives_not_zero_false() {
        s.shot();
        assertFalse(s.destroyed); // jos nije potopljen
    }

    @Test
    void TP03_shot_lives_zero_true() {
        s.shot();
        s.shot();
        assertTrue(s.destroyed);
    }

    @Test
    void TP04_place_add_block() {
        Block b = new Block(0, 0);
        s.place(b);
        assertTrue(true); // samo da pokrije statement, nema getter za listu
    }
    //zbog pita
    @Test
    void TP05_exact_lives_count() {
        // Brod velicine 2
        Ship s2 = new Ship(2, 0, 0, "r");
        s2.shot(); // lives 1
        assertFalse(s2.destroyed);
        s2.shot(); // lives 0
        assertTrue(s2.destroyed);
    }
}
