package Player;

import Ship.Ship;
import Structure.Gridline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class ComputerTest {

    private Computer c;

    @BeforeEach
    void setUp() {
        c = new Computer("AI");
    }

    @Test
    void TP01_createGridMine_creates_7_ships() throws Exception {
        c.createGridMine();
        assertEquals(7, c.getShips().size());
    }

    @Test
    void TP02_createGridMine_has_correct_lengths() throws Exception {
        c.createGridMine();

        List<Integer> lengths = new ArrayList<>();
        for (Ship s : c.getShips()) lengths.add(s.n);
        Collections.sort(lengths);

        assertEquals(Arrays.asList(1,1,2,2,3,4,5), lengths);
    }

    // pokriva if obe grane prvo f pa t da bi videli da li i while radi
    static class FakeGridE extends Gridline {
        int calls = 0;
        boolean marked = false;

        FakeGridE() { super(10, 10); }

        @Override
        public boolean checkShot(int x, int y) {
            calls++;
            return calls >= 2;
        }

        @Override
        public void mark(int x, int y, boolean hit) {
            marked = true;
        }
    }

    @Test
    void TP03_takeAim_executes_and_marks_grid() throws Exception {
        FakeGridE fake = new FakeGridE();
        Field f = Player.class.getDeclaredField("gridE"); //ubacujem fake u polje player za gridE
        f.setAccessible(true);
        f.set(c, fake);

        // meta uvek vraca false ili true, nije bitno bitno je da takeAim prodje
        Player target = new Person("P") {
            @Override
            public boolean shot(int x, int y) { return true; }
        };

        boolean result = c.takeAim(target, 0, 0);

        assertTrue(result);          // jer shot gore vraca true
        assertTrue(fake.calls >= 2); // while se vrteo bar 2 puta if false pa true
        assertTrue(fake.marked);     // mark je pozvan
    }

    @Test
    void TP03_MOCK_takeAim_executes_and_marks_grid() throws Exception {

        Gridline gridE = mock(Gridline.class);
        //da bi prosla wile 2 puta
        when(gridE.checkShot(anyInt(), anyInt())).thenReturn(false, true);
        Field f = Player.class.getDeclaredField("gridE"); //ubaci preko refleksije
        f.setAccessible(true);
        f.set(c, gridE);

        Player target = mock(Player.class);
        when(target.shot(anyInt(), anyInt())).thenReturn(true);

        boolean result = c.takeAim(target, 0, 0);
        assertTrue(result);

        // while se vrteo bar 2 puta
        verify(gridE, atLeast(2)).checkShot(anyInt(), anyInt());

        // mark se poziva jednom tacno kad pogodimo legelan shot
        verify(gridE, times(1)).mark(anyInt(), anyInt(), eq(true));
        verify(target, times(1)).shot(anyInt(), anyInt());
    }
    //dodali zbog pita
    @Test
    void TP04_createGridMine_actually_places_ships_on_grid() throws Exception {
        c.createGridMine();
        Gridline mine = c.gridlineMine();
        boolean foundAnyShip = false;

        // prolazimo kroz grid da vidimo da li je neko polje promenjeno
        // ako je gridM.place(temp) uklonjeno, grid ce biti potpuno prazan  .
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (!c.checkBlockM(x, y).equals(".")) {
                    foundAnyShip = true;
                    break;
                }
            }
        }
        assertTrue(foundAnyShip);
    }

    @Test
    void TP05_takeAim_returns_false_on_miss() throws Exception {

        Gridline mockGridE = mock(Gridline.class);
        when(mockGridE.checkShot(anyInt(), anyInt())).thenReturn(true);

        Field f = Player.class.getDeclaredField("gridE");
        f.setAccessible(true);
        f.set(c, mockGridE);

        Player target = mock(Player.class);
        when(target.shot(anyInt(), anyInt())).thenReturn(false);

        boolean result = c.takeAim(target, 0, 0);

        assertFalse(result);
    }

}