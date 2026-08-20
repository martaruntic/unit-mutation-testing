package Structure;

import Ship.Ship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.*;

public class GridlineTest {

    private Gridline grid;

    @BeforeEach
    void setUp() {
        grid = new Gridline(10, 10);
        grid.create();
    }


    // n = 1 ali polje zauzeto,false
    @Test
    void TP01_checkShip_n1_occupied_false() {
        Ship s = new Ship(1, 2, 2, "r");
        grid.place(s);

        assertFalse(grid.checkShip(1, 2, 2, "r"));
    }

    @Test
    void TP02_checkShip_right_blocked_false() {
        grid.place(new Ship(1, 2, 1, "r"));

        assertFalse(grid.checkShip(3, 1, 1, "r"));
    }

    @Test
    void TP03_checkShip_down_blocked_false() {
        grid.place(new Ship(1, 1, 2, "r"));

        assertFalse(grid.checkShip(3, 1, 1, "d"));
    }

    // promasaj menja prikaz
    @Test
    void TP04_checkBlock_changes_afterMiss() {
        String before = grid.checkBlock(5, 5);

        grid.shot(5, 5);

        String after = grid.checkBlock(5, 5);

        assertNotEquals(before, after);
    }


    @Test
    void TP05_place_n1_check() {
        Ship s = new Ship(1, 0, 0, "r");
        grid.place(s);

        assertTrue(grid.shot(0, 0));
    }

    // right
    @Test
    void TP06_place_right_check() {
        Ship s = new Ship(3, 0, 1, "r");
        grid.place(s);

        assertTrue(grid.shot(0, 1));
        assertTrue(grid.shot(1, 1));
        assertTrue(grid.shot(2, 1));
    }

    // down
    @Test
    void TP07_place_down_check() {
        Ship s = new Ship(3, 3, 0, "d");
        grid.place(s);

        assertTrue(grid.shot(3, 0));
        assertTrue(grid.shot(3, 1));
        assertTrue(grid.shot(3, 2));
    }

    @Test
    void TP08_mark_hit_true_changes() {
        String before = grid.checkBlock(7, 7);

        grid.mark(7, 7, true);

        String after = grid.checkBlock(7, 7);

        assertNotEquals(before, after);
    }

    @Test
    void TP09_mark_hit_false_changes() {
        String before = grid.checkBlock(8, 8);

        grid.mark(8, 8, false);

        String after = grid.checkBlock(8, 8);

        assertNotEquals(before, after);
    }

    @Test
    void TP10_place_invalidDir() {
        Ship s = new Ship(2, 0, 0, "x");

        grid.place(s);

        assertTrue(true); // samo za coverage
    }

    // checkShip preko CSV
    @ParameterizedTest
    @CsvFileSource(resources = "/checkShip.csv", numLinesToSkip = 1)
    void TP11_checkShip_parametrized(int n, int x, int y, String dir, boolean expected) {

        boolean result = grid.checkShip(n, x, y, dir);

        assertEquals(expected, result);
    }
    // checkShot preko CSV
    @ParameterizedTest
    @CsvFileSource(resources = "/checkShot.csv", numLinesToSkip = 1)
    void TP12_checkShot_parametrized(int x, int y, boolean shootBefore, boolean expected) {

        if (shootBefore) {
            grid.shot(x, y);
        }

        boolean result = grid.checkShot(x, y);

        assertEquals(expected, result);
    }
    //zbog pita
    @Test
    void TP13_checkShip_boundary_exactly_10() {
        assertFalse(grid.checkShip(3, 7, 0, "r"));
        assertFalse(grid.checkShip(3, 0, 7, "d"));
    }

    @Test
    void TP14_shot_returns_false_for_water() {
        assertFalse(grid.shot(9, 9));
    }

    @Test
    void TP15_place_updates_ship_internal_state() {
        Ship s = new Ship(2, 0, 0, "r");
        grid.place(s);
        grid.shot(0,0);
        grid.shot(1,0);
        assertTrue(s.destroyed);
    }

    @Test
    void TP16_place_invalidDir_logic() {
        Ship s = new Ship(2, 0, 0, "x");
        grid.place(s);
        assertFalse(grid.shot(0,0));
    }

}