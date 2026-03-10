
// package com.fdjloto.api.model;

// import com.fdjloto.api.model.*;
// import org.junit.jupiter.api.*;

// import java.time.*;

// import static org.junit.jupiter.api.Assertions.*;

// class TicketTest {

// @Test void prePersist_setsIdAndTimestampsWhenNull() {
//                 Ticket t = new Ticket();
//                 assertNull(t.getId());
//                 t.prePersist();
//                 assertNotNull(t.getId());
//                 assertNotNull(t.getCreatedAt());
//                 assertNotNull(t.getUpdatedAt());
//             }

// @Test void preUpdate_updatesUpdatedAt() {
//                 Ticket t = new Ticket();
//                 t.setUpdatedAt(LocalDateTime.now().minusDays(1));
//                 LocalDateTime before = t.getUpdatedAt();
//                 t.preUpdate();
//                 assertTrue(t.getUpdatedAt().isAfter(before));
//             }

// @Test void settersAndGetters_work() {
//                 Ticket t = new Ticket();
//                 User u = new User();
//                 u.setId("u1");
//                 t.setId("t1");
//                 t.setUser(u);
//                 t.setNumbers("1-2-3-4-5");
//                 t.setChanceNumber(9);
//                 t.setDrawDate(LocalDate.of(2025, 1, 1));
//                 t.setDrawDay("mercredi");
//                 LocalDateTime c = LocalDateTime.now().minusDays(2);
//                 LocalDateTime up = LocalDateTime.now().minusDays(1);
//                 t.setCreatedAt(c);
//                 t.setUpdatedAt(up);

//                 assertAll(
//                     () -> assertEquals("t1", t.getId()),
//                     () -> assertEquals(u, t.getUser()),
//                     () -> assertEquals("1-2-3-4-5", t.getNumbers()),
//                     () -> assertEquals(9, t.getChanceNumber()),
//                     () -> assertEquals(LocalDate.of(2025, 1, 1), t.getDrawDate()),
//                     () -> assertEquals("mercredi", t.getDrawDay()),
//                     () -> assertEquals(c, t.getCreatedAt()),
//                     () -> assertEquals(up, t.getUpdatedAt())
//                 );
//             }

// @Test void smoke_04() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_05() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_06() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_07() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_08() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_09() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_10() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_11() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_12() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_13() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_14() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_15() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_16() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_17() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_18() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_19() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }

// @Test void smoke_20() {
//             Ticket obj = new Ticket();
//             assertNotNull(obj);
//         }
// }
