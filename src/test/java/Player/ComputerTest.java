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

    // covers both branches of the if, first false then true, to see whether the while loop also works
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
        Field f = Player.class.getDeclaredField("gridE"); //injecting fake into the player's gridE field
        f.setAccessible(true);
        f.set(c, fake);

        // target always returns false or true, doesn't matter, what matters is that takeAim executes
        Player target = new Person("P") {
            @Override
            public boolean shot(int x, int y) { return true; }
        };

        boolean result = c.takeAim(target, 0, 0);

        assertTrue(result);          // because shot above returns true
        assertTrue(fake.calls >= 2); // while loop ran at least twice, if false then true
        assertTrue(fake.marked);     // mark was called
    }

    @Test
    void TP03_MOCK_takeAim_executes_and_marks_grid() throws Exception {

        Gridline gridE = mock(Gridline.class);
        //so the while loop runs twice
        when(gridE.checkShot(anyInt(), anyInt())).thenReturn(false, true);
        Field f = Player.class.getDeclaredField("gridE"); //inject via reflection
        f.setAccessible(true);
        f.set(c, gridE);

        Player target = mock(Player.class);
        when(target.shot(anyInt(), anyInt())).thenReturn(true);

        boolean result = c.takeAim(target, 0, 0);
        assertTrue(result);

        // while loop ran at least twice
        verify(gridE, atLeast(2)).checkShot(anyInt(), anyInt());

        // mark is called exactly once when we hit a legal shot
        verify(gridE, times(1)).mark(anyInt(), anyInt(), eq(true));
        verify(target, times(1)).shot(anyInt(), anyInt());
    }
    //added for mutation testing (pitest)
    @Test
    void TP04_createGridMine_actually_places_ships_on_grid() throws Exception {
        c.createGridMine();
        Gridline mine = c.gridlineMine();
        boolean foundAnyShip = false;

        // iterate through the grid to see whether any field has changed
        // if gridM.place(temp) is removed, the grid will be completely empty.
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