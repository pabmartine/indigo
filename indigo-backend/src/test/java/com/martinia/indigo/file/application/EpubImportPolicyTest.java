package com.martinia.indigo.file.application;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpubImportPolicyTest {
    @Test void isbn10And13IdentifySameEdition() {
        assertEquals(EpubImportPolicy.isbnKeys(List.of("0-306-40615-2")),
                EpubImportPolicy.isbnKeys(List.of("9780306406157")));
    }
    @Test void normalizeTitleWithoutDependingOnFilename() {
        assertEquals(EpubImportPolicy.title("  EL HÓBBIT:  ilustrado "),
                EpubImportPolicy.title("El Hobbit ilustrado"));
        assertNotEquals(EpubImportPolicy.title("El Hobbit"), EpubImportPolicy.title("El Hobbit ilustrado"));
    }
    @Test void onlyPositiveFiniteHigherVersionsUpgrade() {
        assertTrue(EpubImportPolicy.upgrade(2, 1));
        assertTrue(EpubImportPolicy.upgrade(1, 0));
        assertFalse(EpubImportPolicy.upgrade(1, 1));
        assertFalse(EpubImportPolicy.upgrade(1, 2));
        assertFalse(EpubImportPolicy.upgrade(0, 0));
        assertFalse(EpubImportPolicy.upgrade(Float.NaN, 1));
        assertFalse(EpubImportPolicy.upgrade(Float.POSITIVE_INFINITY, 1));
    }
}
