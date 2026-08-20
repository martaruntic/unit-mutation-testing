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

        // Ovo napravi brodove iz ships.txt
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

        assertEquals(ships.get(0).n, ret); //proverava da li se uvecao brojac unistenih polja
        assertEquals(ships.get(0).n, sc.destroyed); //sta ako je prazna lista
    }

    @Test
    void TP04_allDestroyed_false_when_not_all_destroyed() {

        for (int i = 0; i < 6; i++) {
            ships.get(i).destroyed = true; //unistava prvih 6 brodova
            sc.destroyedUpdate();
        }

        assertFalse(sc.allDestroyed());
        assertFalse(sc.allDestroyed);
    }
//dodali zbog pita
    @Test
    void TP05_allDestroyed_true_when_all_destroyed() {

        for (Ship s : ships) {
            s.destroyed = true; //unistava sve brodove
            sc.destroyedUpdate();
        }

        assertTrue(sc.allDestroyed());
        assertTrue(sc.allDestroyed);
    }
    @Test
    void TP06_allDestroyed_boundary_test() {
        // testiramo tacno 6 brodova ,ne sme biti true
        for (int i = 0; i < 6; i++) {
            ships.get(i).destroyed = true;
            sc.destroyedUpdate();
        }
        assertFalse(sc.allDestroyed());

        // testiramo 7 brod
        ships.get(6).destroyed = true;
        sc.destroyedUpdate();
        assertTrue(sc.allDestroyed());
    }
    @Test
    void TP07_getDestroyed_returns_actual_data() {
        Collection<Boolean> destroyedValues = sc.getDestroyed();
        //proveri da nije null
        assertNotNull(destroyedValues);

        // provera velicine mora biti 7
        // ako mutant vrati emptyList, size ce biti 0 i test ce pasti , ubijen mutant
        assertEquals(7, destroyedValues.size());

        // provera sadrzaja na pocetku su svi false
        for(Boolean b : destroyedValues) {
            assertFalse(b);
        }
    }
}
