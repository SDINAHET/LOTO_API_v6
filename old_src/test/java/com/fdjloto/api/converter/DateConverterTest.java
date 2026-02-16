
package com.fdjloto.api.converter;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateConverterTest {

    @Test void convertToEntityAttribute_parsesValidDate() {
        DateConverter c = new DateConverter();
        LocalDate d = c.convertToEntityAttribute("2025-01-02");
        assertEquals(LocalDate.of(2025,1,2), d);
    }

    @Test void convertToDatabaseColumn_formatsValidDate() {
        DateConverter c = new DateConverter();
        String s = c.convertToDatabaseColumn(LocalDate.of(2025,1,2));
        assertEquals("2025-01-02", s);
    }

    @Test void convertToEntityAttribute_nullReturnsNull() {
        DateConverter c = new DateConverter();
        assertNull(c.convertToEntityAttribute(null));
    }

    @Test void convertToDatabaseColumn_nullReturnsNull() {
        DateConverter c = new DateConverter();
        assertNull(c.convertToDatabaseColumn(null));
    }

    @Test void multipleCalls_areIdempotent() {
        DateConverter c = new DateConverter();
        String s1 = c.convertToDatabaseColumn(LocalDate.of(2024,12,31));
        String s2 = c.convertToDatabaseColumn(LocalDate.of(2024,12,31));
        assertEquals(s1, s2);
    }
}
