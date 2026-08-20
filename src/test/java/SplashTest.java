import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class SplashTest {

    @BeforeAll
    static void ensureNotHeadless() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
                "GUI tests require non-headless environment.");
    }

    @BeforeEach
    void resetReady() {
        Splash.ready = true;
    }

    @AfterEach
    void cleanup() throws Exception {
        // zatvori window ako je ostao vidljiv
        // (ne znamo koji test je prošao/pa koristimo reflection safe)
    }

    @Test
    void SPL01_runImage_index1_readyTrue_showsWindow_and_startsTimerImg() throws Exception {
        Ship.ShipCount ship = new Ship.ShipCount(new Player.Person("X")); // real shipcount
        Frame owner = new JFrame();

        Splash splash = new Splash(1, ship, owner);

        // ready == true -> treba da postavi ready=false, window visible, timerImg start
        runOnEDT(() -> splash.runImage(1));

        JWindow window = (JWindow) getField(splash, "window");
        Timer timerImg = (Timer) getField(splash, "timerImg");

        assertFalse(Splash.ready);
        assertTrue(window.isVisible());
        assertTrue(timerImg.isRunning());

        runOnEDT(window::dispose);
    }

    @Test
    void SPL02_runImage_index2_readyFalse_startsTimerWait() throws Exception {
        Ship.ShipCount ship = new Ship.ShipCount(new Player.Person("X"));
        Frame owner = new JFrame();

        Splash splash = new Splash(2, ship, owner);

        // forsiraj ready=false pre runImage
        Splash.ready = false;

        runOnEDT(() -> splash.runImage(2));

        Timer timerWait = (Timer) getField(splash, "timerWait");
        assertTrue(timerWait.isRunning(), "Kad je ready=false, runImage treba da startuje timerWait");

        // počisti
        JWindow window = (JWindow) getField(splash, "window");
        runOnEDT(window::dispose);
    }

    @Test
    void SPL03_actionPerformed_timerWaitRunning_and_readyTrue_branch() throws Exception {
        Ship.ShipCount ship = new Ship.ShipCount(new Player.Person("X"));
        Frame owner = new JFrame();
        Splash splash = new Splash(1, ship, owner);

        JWindow window = (JWindow) getField(splash, "window");
        Timer timerWait = (Timer) getField(splash, "timerWait");
        Timer timerImg = (Timer) getField(splash, "timerImg");

        // priprema uslova: timerWait.isRunning()==true i ready==true
        Splash.ready = true;
        runOnEDT(timerWait::start);
        assertTrue(timerWait.isRunning());

        // okini actionPerformed
        runOnEDT(() -> splash.actionPerformed(new ActionEvent(this, 0, "tick")));

        assertFalse(Splash.ready, "U ovoj grani ready postaje false");
        assertTrue(window.isVisible(), "U ovoj grani window postaje visible");
        assertFalse(timerWait.isRunning(), "timerWait treba da se stopuje");
        assertTrue(timerImg.isRunning(), "timerImg treba da se startuje");

        runOnEDT(window::dispose);
    }

    @Test
    void SPL04_actionPerformed_timerImgRunning_branch_hidesWindow_setsReadyTrue() throws Exception {
        Ship.ShipCount ship = new Ship.ShipCount(new Player.Person("X"));
        Frame owner = new JFrame();
        Splash splash = new Splash(1, ship, owner);

        JWindow window = (JWindow) getField(splash, "window");
        Timer timerImg = (Timer) getField(splash, "timerImg");
        Timer timerWait = (Timer) getField(splash, "timerWait");

        // priprema: timerImg.isRunning()==true
        Splash.ready = false;
        runOnEDT(() -> {
            window.setVisible(true);
            timerImg.start();
        });
        assertTrue(timerImg.isRunning());

        // okini actionPerformed -> else if(timerImg.isRunning()) grana
        runOnEDT(() -> splash.actionPerformed(new ActionEvent(this, 0, "tick2")));

        assertTrue(Splash.ready, "Posle ove grane ready treba da bude true");
        assertFalse(window.isVisible(), "Prozor treba da se sakrije");
        assertFalse(timerImg.isRunning(), "timerImg stop");
        assertFalse(timerWait.isRunning(), "timerWait stop");

        runOnEDT(window::dispose);
    }

    @Test
    void SPL05_run_coversWhileAndIf_thenExits() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

        Player.Person p = new Player.Person("X");
        Ship.ShipCount ship = new Ship.ShipCount(p);

        // napravi Splash (frame owner može JFrame)
        JFrame owner = new JFrame();
        Splash splash = new Splash(1, ship, owner);

        // start thread
        splash.start();

        // Forsiraj da uđe u if(ship.destroyed != 0)
        ship.destroyed = 1;

        // Pusti malo vremena da thread uđe u petlju i obradi
        Thread.sleep(50);

        // Sada prekini while petlju
        ship.allDestroyed = true;

        // Sačekaj da thread završi
        splash.join(500);

        assertFalse(splash.isAlive(), "Splash thread should exit when allDestroyed becomes true");

        // počisti prozor
        // (ako je bio vidljiv)
        JWindow window = (JWindow) getField(splash, "window");
        runOnEDT(window::dispose);
    }

    @Test
    void SPL08_actionPerformed_noBranch_taken_when_noTimersRunning() throws Exception {
        Player.Person p = new Player.Person("X");
        Ship.ShipCount ship = new Ship.ShipCount(p);
        Splash splash = new Splash(1, ship, new JFrame());

        Timer timerWait = (Timer) getField(splash, "timerWait");
        Timer timerImg = (Timer) getField(splash, "timerImg");
        JWindow window = (JWindow) getField(splash, "window");

        // Forsiraj da NIŠTA ne radi:
        // oba timera stop + ready može biti bilo šta, ali neka bude false da prvi if sigurno padne
        Splash.ready = false;

        runOnEDT(() -> {
            timerWait.stop();
            timerImg.stop();
            window.setVisible(false);
        });

        assertFalse(timerWait.isRunning());
        assertFalse(timerImg.isRunning());

        // pozovi actionPerformed -> ne ulazi ni u if ni u else-if
        boolean readyBefore = Splash.ready;
        boolean visibleBefore = window.isVisible();

        runOnEDT(() -> splash.actionPerformed(new java.awt.event.ActionEvent(this, 3, "noop")));

        // ništa se ne menja
        assertEquals(readyBefore, Splash.ready);
        assertEquals(visibleBefore, window.isVisible());
        assertFalse(timerWait.isRunning());
        assertFalse(timerImg.isRunning());

        runOnEDT(window::dispose);
    }
    @Test
    void SPL10_actionPerformed_timerWaitRunning_but_readyFalse() throws Exception {
        Player.Person p = new Player.Person("X");
        Ship.ShipCount ship = new Ship.ShipCount(p);

        Splash splash = new Splash(1, ship, new JFrame());

        Timer timerWait = (Timer) getField(splash, "timerWait");
        Timer timerImg = (Timer) getField(splash, "timerImg");
        JWindow window = (JWindow) getField(splash, "window");

        // KLJUČNO: ready = false
        Splash.ready = false;

        runOnEDT(() -> {
            timerWait.start();   // true
            timerImg.stop();     // false
            window.setVisible(false);
        });

        assertTrue(timerWait.isRunning());
        assertFalse(timerImg.isRunning());

        boolean before = Splash.ready;

        runOnEDT(() ->
                splash.actionPerformed(new java.awt.event.ActionEvent(this, 5, "x"))
        );

        // ništa se ne sme promeniti
        assertEquals(before, Splash.ready);
        assertFalse(window.isVisible());

        runOnEDT(window::dispose);
    }

    @Test
    void SPL11_run_coversIfFalse_when_destroyedIsZero() throws Exception {
        Player.Person p = new Player.Person("X");
        Ship.ShipCount ship = new Ship.ShipCount(p);

        Splash splash = new Splash(1, ship, new JFrame());

        // destroyed je 0 po defaultu -> treba da pokrije if(FALSE)
        splash.start();

        // pusti malo da uđe u while i proveri if
        Thread.sleep(30);

        // prekini while
        ship.allDestroyed = true;

        splash.join(500);
        assertFalse(splash.isAlive());

        JWindow window = (JWindow) getField(splash, "window");
        runOnEDT(window::dispose);
    }


    // ---------- helpers ----------
    private static void runOnEDT(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeAndWait(r);
    }

    private static Object getField(Object obj, String fieldName) throws Exception {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(obj);
    }
}