package FileExceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FileExceptionsTest {
    @Test
    void testBadFileException() {
        // Ako nema konstruktor sa Stringom, koristi prazan:
        BadFileException ex = new BadFileException(1);
        assertNotNull(ex);
    }

    // Ako imaš i FileException, dodaj ga ovako:
    /*
    @Test
    void testFileException() {
        FileException ex = new FileException("Greska u fajlu");
        assertNotNull(ex);
    }
    */
}