
// package com.fdjloto.api.converter;

// import org.junit.jupiter.api.Test;

// import java.sql.Timestamp;
// import java.time.LocalDateTime;

// import static org.junit.jupiter.api.Assertions.*;

// class LocalDateTimeAttributeConverterTest {

//     @Test void convertToDatabaseColumn_nullReturnsNull() {
//         LocalDateTimeAttributeConverter c = new LocalDateTimeAttributeConverter();
//         assertNull(c.convertToDatabaseColumn(null));
//     }

//     @Test void convertToEntityAttribute_nullReturnsNull() {
//         LocalDateTimeAttributeConverter c = new LocalDateTimeAttributeConverter();
//         assertNull(c.convertToEntityAttribute(null));
//     }

//     @Test void roundTrip_preservesDateTime() {
//         LocalDateTimeAttributeConverter c = new LocalDateTimeAttributeConverter();
//         LocalDateTime now = LocalDateTime.of(2025, 1, 2, 3, 4, 5);
//         Timestamp ts = c.convertToDatabaseColumn(now);
//         assertNotNull(ts);
//         LocalDateTime restored = c.convertToEntityAttribute(ts);
//         assertEquals(now, restored);
//     }
// }
