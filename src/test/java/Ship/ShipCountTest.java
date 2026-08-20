package Ship;

import Player.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ShipCountTest {

    private ShipCount sc;
    private List<Ship> ships;

    @BeforeEach
    void setUp() throws Exception {

        Person p = new Person("Test");

        // This creates the ships from ships.txt
        p.createGridMine();

        ships = p.getShips();
        sc = new ShipCount(p);
    }

    @Test
    void TP01_constructor_initial_state() {
        assertEquals(0, sc.destroyed);
        assertFalse(sc.allDestroyed);

        for (Boolean b : sc.getDestroyed()) {
            assertFalse(b);
        }
    }

    @Test
    void TP02_destroyedUpdate_when_nothing_destroyed() {
        assertEquals(-1, sc.destroyedUpdate());
    }

    @Test
    void TP03_destroyedUpdate_when_one_ship_destroyed() {

        ships.get(0).destroyed = true;

        int ret = sc.destroyedUpdate();

        assertEquals(ships.get(0).n, ret); //checks whether the destroyed-fields counter increased
        assertEquals(ships.get(0).n, sc.destroyed); //what if the list is empty
    }

    @Test
    void TP04_allDestroyed_false_when_not_all_destroyed() {

        for (int i = 0; i < 6; i++) {
            ships.get(i).destroyed = true; //destroys the first 6 ships
            sc.destroyedUpdate();
        }

        assertFalse(sc.allDestroyed());
        assertFalse(sc.allDestroyed);
    }
//added for mutation testing (pitest)
    @Test
    void TP05_allDestroyed_true_when_all_destroyed() {

        for (Ship s : ships) {
            s.destroyed = true; //destroys all ships
            sc.destroyedUpdate();
        }

        assertTrue(sc.allDestroyed());
        assertTrue(sc.allDestroyed);
    }
    @Test
    void TP06_allDestroyed_boundary_test() {
        // testing exactly 6 ships, must not be true
        for (int i = 0; i < 6; i++) {
            ships.get(i).destroyed = true;
            sc.destroyedUpdate();
        }
        assertFalse(sc.allDestroyed());

        // testing the 7th ship
        ships.get(6).destroyed = true;
        sc.destroyedUpdate();
        assertTrue(sc.allDestroyed());
    }
    @Test
    void TP07_getDestroyed_returns_actual_data() {
        Collection<Boolean> destroyedValues = sc.getDestroyed();
        //check that it's not null
        assertNotNull(destroyedValues);

        // check that the size must be 7
        // if the mutant returns an empty list, size will be 0 and the test will fail, mutant killed
        assertEquals(7, destroyedValues.size());

        // check the contents, initially all false
        for(Boolean b : destroyedValues) {
            assertFalse(b);
        }
    }
}
