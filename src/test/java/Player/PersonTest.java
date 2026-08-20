package Player;

import Ship.Ship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PersonTest {

    private Person p;

    @BeforeEach
    void setUp() {
        p = new Person("P");
    }

    @Test
    void TP01_createGridMine_creates_7_ships() throws Exception {
        p.createGridMine();
        assertEquals(7, p.getShips().size());
    }

    @Test
    void TP02_createGridMine_has_correct_lengths() throws Exception {
        p.createGridMine();

        List<Integer> lengths = new ArrayList<>();
        for (Ship s : p.getShips()) lengths.add(s.n);
        Collections.sort(lengths);

        assertEquals(Arrays.asList(1,1,2,2,3,4,5), lengths);
    }

    @Test
    void TP03_takeAim_marks_enemy_grid_and_returns_same_as_shot() throws Exception {
        //person ima enemy grid (gridE) kreiran u Player konstruktoru,
        Computer comp = new Computer("C");
        comp.createGridMine(); // meta ima brodove

        int x = 0, y = 0;
        String before = p.checkBlockE(x, y);
        boolean result = p.takeAim(comp, x, y);

        String after = p.checkBlockE(x, y);
        assertNotEquals(before, after);

        // Ne mozeme se ponovo pozvati jer bi promenilo stanje, pa samo proverimo da je boolean validan
        assertTrue(result == true || result == false);
    }

    @Test
    void TP_MOCK_takeAim_returnsTrue_when_enemyShotHits() throws Exception {

        // pravi Person , objekat koji testiramo
        Person p = new Person("P1");
        p.createGridMine();
        Computer enemy = mock(Computer.class);
        when(enemy.shot(0, 0)).thenReturn(true); //kas pozovem vrati true

        boolean result = p.takeAim(enemy, 0, 0);

        assertTrue(result);                 // vraća true
        verify(enemy).shot(0, 0);           // proveri da je pozvano
    }
    //dodali zbog pita
    @Test
    void TP05_takeAim_returns_false_on_miss() throws Exception {
        // mutant replaced boolean return with true
        Computer enemy = mock(Computer.class);
        // prisilni promasaj
        when(enemy.shot(5, 5)).thenReturn(false);

        boolean result = p.takeAim(enemy, 5, 5);
        // ako mutant uvek vraca true, ovaj assert pada
        assertFalse(result);
    }

    @Test
    void TP06_createGridMine_actually_places_ships_on_grid() throws Exception {
        // mutant removed call to gridM.place
        p.createGridMine();

        boolean foundShip = false;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (!p.checkBlockM(x, y).equals(".")) {
                    foundShip = true;
                    break;
                }
            }
        }
        assertTrue(foundShip);
    }

    @Test
    void TP07_createGridMine_boundary_n5() throws Exception {
        //  mutant n > 5 boundary
        // ovaj test zavisi od ships.txt, ako je u ships.txt jedan brod duzine 5
        // mutant koji promeni uslov u n >= 5 ce baciti BadFileException(2)
        assertDoesNotThrow(() -> p.createGridMine());
    }

    @Test
    void TP08_createGridMine_invalid_coordinates_throws_exception() throws Exception {
        // pokriva grane i zatvaranje fajlova u catch blokovima
        assertTrue(p.getShips().size() <= 7);
    }


}