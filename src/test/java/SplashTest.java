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
        // close the window if it remained visible
        // (we don't know which test ran, so we use a reflection-safe approach)
    }

    @Test
    void SPL01_runImage_index1_readyTrue_showsWindow_and_startsTimerImg() throws Exception {
        Ship.ShipCount ship = new Ship.ShipCount(new Player.Person("X")); // real shipcount
        Frame owner = new JFrame();

        Splash splash = new Splash(1, ship, owner);

        // ready == true -> should set ready=false, window visible, timerImg start
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

        // force ready=false before runImage
        Splash.ready = false;

        runOnEDT(() -> splash.runImage(2));

        Timer timerWait = (Timer) getField(splash, "timerWait");
        assertTrue(timerWait.isRunning(), "When ready=false, runImage should start timerWait");

        // cleanup
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

        // prepare conditions: timerWait.isRunning()==true and ready==true
        Splash.ready = true;
        runOnEDT(timerWait::start);
        assertTrue(timerWait.isRunning());

        // trigger actionPerformed
        runOnEDT(() -> splash.actionPerformed(new ActionEvent(this, 0, "tick")));

        assertFalse(Splash.ready, "In this branch ready becomes false");
        assertTrue(window.isVisible(), "In this branch window becomes visible");
        assertFalse(timerWait.isRunning(), "timerWait should be stopped");
        assertTrue(timerImg.isRunning(), "timerImg should be started");

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

        // prepare: timerImg.isRunning()==true
        Splash.ready = false;
        runOnEDT(() -> {
            window.setVisible(true);
            timerImg.start();
        });
        assertTrue(timerImg.isRunning());

        // trigger actionPerformed -> else if(timerImg.isRunning()) branch
        runOnEDT(() -> splash.actionPerformed(new ActionEvent(this, 0, "tick2")));

        assertTrue(Splash.ready, "After this branch ready should be true");
        assertFalse(window.isVisible(), "The window should be hidden");
        assertFalse(timerImg.isRunning(), "timerImg stop");
        assertFalse(timerWait.isRunning(), "timerWait stop");

        runOnEDT(window::dispose);
    }

    @Test
    void SPL05_run_coversWhileAndIf_thenExits() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

        Player.Person p = new Player.Person("X");
        Ship.ShipCount ship = new Ship.ShipCount(p);

        // create Splash (frame owner can be a JFrame)
        JFrame owner = new JFrame();
        Splash splash = new Splash(1, ship, owner);

        // start thread
        splash.start();

        // Force it to enter if(ship.destroyed != 0)
        ship.destroyed = 1;

        // Give the thread a little time to enter the loop and process
        Thread.sleep(50);

        // Now break the while loop
        ship.allDestroyed = true;

        // Wait for the thread to finish
        splash.join(500);

        assertFalse(splash.isAlive(), "Splash thread should exit when allDestroyed becomes true");

        // clean up the window
        // (if it was visible)
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

        // Force NOTHING to happen:
        // both timers stopped + ready can be anything, but let it be false so the first if surely fails
        Splash.ready = false;

        runOnEDT(() -> {
            timerWait.stop();
            timerImg.stop();
            window.setVisible(false);
        });

        assertFalse(timerWait.isRunning());
        assertFalse(timerImg.isRunning());

        // call actionPerformed -> does not enter either the if or the else-if
        boolean readyBefore = Splash.ready;
        boolean visibleBefore = window.isVisible();

        runOnEDT(() -> splash.actionPerformed(new java.awt.event.ActionEvent(this, 3, "noop")));

        // nothing changes
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

        // CRUCIAL: ready = false
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

        // nothing should change
        assertEquals(before, Splash.ready);
        assertFalse(window.isVisible());

        runOnEDT(window::dispose);
    }

    @Test
    void SPL11_run_coversIfFalse_when_destroyedIsZero() throws Exception {
        Player.Person p = new Player.Person("X");
        Ship.ShipCount ship = new Ship.ShipCount(p);

        Splash splash = new Splash(1, ship, new JFrame());

        // destroyed is 0 by default -> should cover if(FALSE)
        splash.start();

        // give it a little time to enter the while loop and check the if
        Thread.sleep(30);

        // break the while loop
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