package FileExceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FileExceptionsTest {
    @Test
    void testBadFileException() {
        // If there's no constructor with a String, use the empty one:
        BadFileException ex = new BadFileException(1);
        assertNotNull(ex);
    }

    // If you also have FileException, add it like this:
    /*
    @Test
    void testFileException() {
        FileException ex = new FileException("File error");
        assertNotNull(ex);
    }
    */
}