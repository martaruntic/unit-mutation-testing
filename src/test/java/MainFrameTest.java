import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MainFrameTest {

    @BeforeAll
    static void ensureNotHeadless() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
                "GUI tests require non-headless environment.");
    }

    @AfterEach
    void disposeFrameAndResetSingleton() throws Exception {
        MainFrame mf = MainFrame.getMainFrame();
        JFrame frame = (JFrame) getField(mf, "frame");
        if (frame != null) {
            runOnEDT(frame::dispose);
        }
        setField(mf, "endGame", false);
    }

    // ---------------------------------------------------------
    // 1) startMain + basic GUI init
    // ---------------------------------------------------------
    @Test
    void GUI01_startMain_buildsUI_initialStates_ok() throws Exception {
        MainFrame mf = MainFrame.getMainFrame();

        boolean ok = callOnEDT(() -> mf.startMain("Protagonist", "Antagonist"));
        assertTrue(ok);

        JFrame frame = (JFrame) getField(mf, "frame");
        assertNotNull(frame);
        assertTrue(frame.isVisible());

        JButton[][] buttonM = (JButton[][]) getField(mf, "buttonM");
        JButton[][] buttonE = (JButton[][]) getField(mf, "buttonE");
        JTextArea text = (JTextArea) getField(mf, "text");
        JTable table = (JTable) getField(mf, "table");

        assertFalse(buttonM[0][0].isEnabled());
        assertTrue(buttonE[0][0].isEnabled());
        assertEquals("00", buttonE[0][0].getActionCommand());
        assertTrue(text.getText().contains("Click any button"));

        assertEquals(7, table.getRowCount());
        assertEquals(3, table.getColumnCount());
    }

    // ---------------------------------------------------------
    // 2) startSplash1 / startSplash2 coverage
    // ---------------------------------------------------------
    @Test
    void GUI02_startSplash1_and_startSplash2_returnSplash() throws Exception {
        MainFrame mf = MainFrame.getMainFrame();
        assertTrue(callOnEDT(() -> mf.startMain("P1", "P2")));

        Splash s1 = callOnEDT(mf::startSplash1);
        Splash s2 = callOnEDT(mf::startSplash2);

        assertNotNull(s1);
        assertNotNull(s2);
    }

    // ---------------------------------------------------------
    // 3) Forsiraj actionPerformed grane: hit/miss + destroyedUpdate != -1/-1
    // + changeButtonE boja za X i plus
    // + changeButtonM boje za X i +
    // ---------------------------------------------------------
    @Test
    void GUI03_actionPerformed_forcedBranches_hit_miss_destroyedUpdate_colors() throws Exception {
        MainFrame mf = MainFrame.getMainFrame();
        assertTrue(callOnEDT(() -> mf.startMain("P1", "P2")));

        JButton[][] buttonE = (JButton[][]) getField(mf, "buttonE");
        JButton[][] buttonM = (JButton[][]) getField(mf, "buttonM");
        JTextArea text = (JTextArea) getField(mf, "text");

        // CASE A:
        // play1.takeAim = true (hit) + checkBlockE="X" -> green
        // play2.takeAim = false (miss)
        // count2.destroyedUpdate = 3 (append "ship destroyed")
        // count1.destroyedUpdate = -1 (ne appenduje)
        Player.Person forcedP1_A = new Player.Person("P1") {
            @Override public boolean takeAim(Player.Player enemy, int x, int y) { return true; }
            @Override public String checkBlockE(int x, int y) { return "X"; }
        };

        Player.Computer forcedP2_A = new Player.Computer("P2") {
            @Override public boolean takeAim(Player.Player enemy, int x, int y) { return false; }

            // changeButtonM čita play2.checkBlockE(j,i)
            @Override public String checkBlockE(int x, int y) {
                if (x == 0 && y == 0) return "X"; // pink
                if (x == 1 && y == 0) return "+"; // purple
                return ".";
            }
        };

        setField(mf, "play1", forcedP1_A);
        setField(mf, "play2", forcedP2_A);
        setField(mf, "count2", new FakeShipCountForTable(forcedP2_A, 3, false));
        setField(mf, "count1", new FakeShipCountForTable(forcedP1_A, -1, false));

        runOnEDT(() -> buttonE[0][0].doClick());

        assertTrue(text.getText().contains("successful shot"));
        assertTrue(text.getText().contains("length ship was destroyed")); // count2=3

        assertEquals("X", buttonE[0][0].getText());
        assertEquals(new Color(170,255,153), buttonE[0][0].getBackground()); // green

        assertEquals("X", buttonM[0][0].getText());
        assertEquals(new Color(255,179,204), buttonM[0][0].getBackground()); // pink
        assertEquals("+", buttonM[1][0].getText());
        assertEquals(new Color(234,230,255), buttonM[1][0].getBackground()); // purple

        // CASE B:
        // play1.takeAim = false (miss) + checkBlockE="+" -> blue
        // play2.takeAim = true (hit)
        // count2.destroyedUpdate = -1 (ne append)
        // count1.destroyedUpdate = 4 (append)
        runOnEDT(() -> text.setText(""));

        Player.Person forcedP1_B = new Player.Person("P1") {
            @Override public boolean takeAim(Player.Player enemy, int x, int y) { return false; }
            @Override public String checkBlockE(int x, int y) { return "+"; }
        };

        Player.Computer forcedP2_B = new Player.Computer("P2") {
            @Override public boolean takeAim(Player.Player enemy, int x, int y) { return true; }
            @Override public String checkBlockE(int x, int y) { return "."; }
        };

        setField(mf, "play1", forcedP1_B);
        setField(mf, "play2", forcedP2_B);
        setField(mf, "count2", new FakeShipCountForTable(forcedP2_B, -1, false));
        setField(mf, "count1", new FakeShipCountForTable(forcedP1_B, 4, false));

        runOnEDT(() -> buttonE[1][1].doClick());

        assertTrue(text.getText().contains("missed the shot"));
        assertTrue(text.getText().contains("length ship was destroyed")); // count1=4

        assertEquals("+", buttonE[1][1].getText());
        assertEquals(new Color(204,247,255), buttonE[1][1].getBackground()); // blue
    }

    // ---------------------------------------------------------
    // 4) checkEnd: draw / play2 wins / play1 wins (3 grane)
    // ---------------------------------------------------------
    @Test
    void GUI04_checkEnd_draw_branch() throws Exception {
        MainFrame mf = MainFrame.getMainFrame();
        assertTrue(callOnEDT(() -> mf.startMain("P1", "P2")));

        JTextArea text = (JTextArea) getField(mf, "text");

        // oba true -> draw
        setField(mf, "count1", new FakeShipCountForTable(new Player.Person("X"), -1, true));
        setField(mf, "count2", new FakeShipCountForTable(new Player.Person("Y"), -1, true));

        runOnEDT(() -> {
            try {
                invokePrivate(mf, "checkEnd");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue((boolean) getField(mf, "endGame"));
        assertTrue(text.getText().contains("draw"));
    }

    @Test
    void GUI05_checkEnd_play2Wins_branch() throws Exception {
        MainFrame mf = MainFrame.getMainFrame();
        assertTrue(callOnEDT(() -> mf.startMain("P1", "P2")));

        JTextArea text = (JTextArea) getField(mf, "text");

        // count1 true, count2 false -> play2 wins
        setField(mf, "count1", new FakeShipCountForTable(new Player.Person("X"), -1, true));
        setField(mf, "count2", new FakeShipCountForTable(new Player.Person("Y"), -1, false));

        runOnEDT(() -> {
            try {
                invokePrivate(mf, "checkEnd");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue((boolean) getField(mf, "endGame"));
        assertTrue(text.getText().contains("wins"));
    }

    @Test
    void GUI06_checkEnd_play1Wins_branch() throws Exception {
        MainFrame mf = MainFrame.getMainFrame();
        assertTrue(callOnEDT(() -> mf.startMain("P1", "P2")));

        JTextArea text = (JTextArea) getField(mf, "text");

        // count2 true, count1 false -> play1 wins
        setField(mf, "count1", new FakeShipCountForTable(new Player.Person("X"), -1, false));
        setField(mf, "count2", new FakeShipCountForTable(new Player.Person("Y"), -1, true));

        runOnEDT(() -> {
            try {
                invokePrivate(mf, "checkEnd");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue((boolean) getField(mf, "endGame"));
        assertTrue(text.getText().contains("wins"));
    }

    // ---------------------------------------------------------
    // 5) endGame==true -> actionPerformed return odmah
    // ---------------------------------------------------------
    @Test
    void GUI07_endGameTrue_actionPerformed_returnsImmediately_noChanges() throws Exception {
        MainFrame mf = MainFrame.getMainFrame();
        assertTrue(callOnEDT(() -> mf.startMain("P1", "P2")));

        JButton[][] buttonE = (JButton[][]) getField(mf, "buttonE");
        JTextArea text = (JTextArea) getField(mf, "text");

        setField(mf, "endGame", true);

        String before = text.getText();
        assertTrue(buttonE[2][2].isEnabled());

        runOnEDT(() -> buttonE[2][2].doClick());

        assertEquals(before, text.getText());
        assertTrue(buttonE[2][2].isEnabled());
    }

    // =========================================================
    // FakeShipCountForTable: KLJUČNO da getDestroyed() ima 7 elemenata
    // =========================================================
    static class FakeShipCountForTable extends Ship.ShipCount {
        private final int destroyedUpdateRet;
        private final boolean allDestroyedRet;

        FakeShipCountForTable(Player.Player owner, int destroyedUpdateRet, boolean allDestroyedRet) {
            super(owner);
            this.destroyedUpdateRet = destroyedUpdateRet;
            this.allDestroyedRet = allDestroyedRet;
        }

        @Override public int destroyedUpdate() { return destroyedUpdateRet; }

        @Override public boolean allDestroyed() { return allDestroyedRet; }

        @Override public List<Boolean> getDestroyed() {
            // 7 redova u tabeli -> 7 boolean vrednosti
            return Arrays.asList(false, false, false, false, false, false, false);
        }
    }

    // ======================= EDT helpers =======================
    private static void runOnEDT(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeAndWait(r);
    }

    private static <T> T callOnEDT(EDTCallable<T> c) throws Exception {
        final Object[] res = new Object[1];
        final Exception[] ex = new Exception[1];

        runOnEDT(() -> {
            try { res[0] = c.call(); }
            catch (Exception e) { ex[0] = e; }
        });

        if (ex[0] != null) throw ex[0];
        @SuppressWarnings("unchecked")
        T out = (T) res[0];
        return out;
    }

    @Test
    void GUI04_MOCK_checkEnd_draw_branch() throws Exception {
        MainFrame mf = MainFrame.getMainFrame();
        assertTrue(callOnEDT(() -> mf.startMain("P1", "P2")));

        JTextArea text = (JTextArea) getField(mf, "text");

        // MOCK: count1 i count2
        Ship.ShipCount count1 = mock(Ship.ShipCount.class);
        Ship.ShipCount count2 = mock(Ship.ShipCount.class);

        // oba allDestroyed() true -> draw grana
        when(count1.allDestroyed()).thenReturn(true);
        when(count2.allDestroyed()).thenReturn(true);

        // ubaci mock-ove u MainFrame
        setField(mf, "count1", count1);
        setField(mf, "count2", count2);

        // pozovi private metodu checkEnd()
        runOnEDT(() -> {
            try {
                invokePrivate(mf, "checkEnd");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // ASSERT
        assertTrue((boolean) getField(mf, "endGame"));
        assertTrue(text.getText().contains("draw"));

        // VERIFY (da profesor vidi Mockito poentu)
        verify(count1, atLeastOnce()).allDestroyed();
        verify(count2, atLeastOnce()).allDestroyed();
    }

    @FunctionalInterface
    private interface EDTCallable<T> { T call() throws Exception; }

    // ===================== Reflection helpers ===================
    private static Object getField(Object obj, String fieldName) throws Exception {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(obj);
    }

    private static void setField(Object obj, String fieldName, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private static void invokePrivate(Object obj, String methodName) throws Exception {
        Method m = obj.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(obj);
    }
}