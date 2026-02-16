
// package com.fdjloto.api.dto;

// import com.fdjloto.api.TestDataFactory;
// import com.fdjloto.api.model.*;
// import org.junit.jupiter.api.*;

// import java.time.*;

// import static org.junit.jupiter.api.Assertions.*;

// class TicketDTOTest {
// @Test void ctor_fromTicket_populatesFields() {
//                 User u = TestDataFactory.user("u1", false);
//                 Ticket t = TestDataFactory.ticket("t1", u);
//                 TicketDTO dto = new TicketDTO(t);
//                 assertEquals("t1", dto.getId());
//                 assertEquals("u1", dto.getUserId());
//                 assertEquals("1-2-3-4-5", dto.getNumbers());
//                 assertEquals("7", dto.getChanceNumber());
//                 assertNotNull(dto.getDrawDate());
//                 assertNotNull(dto.getCreatedAt());
//                 assertNotNull(dto.getUpdatedAt());
//             }

// @Test void ctor_fromTicket_handlesNullDates() {
//                 User u = TestDataFactory.user("u1", false);
//                 Ticket t = new Ticket();
//                 t.setId("t1");
//                 t.setUser(u);
//                 t.setNumbers("1-2-3-4-5");
//                 t.setChanceNumber(1);
//                 t.setDrawDate(null);
//                 t.setCreatedAt(null);
//                 t.setUpdatedAt(null);
//                 TicketDTO dto = new TicketDTO(t);
//                 assertNull(dto.getDrawDate());
//                 assertEquals("N/A", dto.getCreatedAt());
//                 assertNull(dto.getUpdatedAt());
//             }

// @Test void settersAndGetters_work() {
//                 TicketDTO dto = new TicketDTO();
//                 dto.setId("id");
//                 dto.setUserId("u1");
//                 dto.setNumbers("a");
//                 dto.setChanceNumber("2");
//                 dto.setDrawDate("2025-01-01");
//                 dto.setCreatedAt("c");
//                 dto.setUpdatedAt("u");
//                 assertEquals("id", dto.getId());
//                 assertEquals("u1", dto.getUserId());
//                 assertEquals("a", dto.getNumbers());
//                 assertEquals("2", dto.getChanceNumber());
//                 assertEquals("2025-01-01", dto.getDrawDate());
//                 assertEquals("c", dto.getCreatedAt());
//                 assertEquals("u", dto.getUpdatedAt());
//             }

// @Test void smoke_04() {
//             TicketDTO obj = new TicketDTO();
//             assertNotNull(obj);
//         }

// @Test void smoke_05() {
//             TicketDTO obj = new TicketDTO();
//             assertNotNull(obj);
//         }

// @Test void smoke_06() {
//             TicketDTO obj = new TicketDTO();
//             assertNotNull(obj);
//         }

// @Test void smoke_07() {
//             TicketDTO obj = new TicketDTO();
//             assertNotNull(obj);
//         }

// @Test void smoke_08() {
//             TicketDTO obj = new TicketDTO();
//             assertNotNull(obj);
//         }

// @Test void smoke_09() {
//             TicketDTO obj = new TicketDTO();
//             assertNotNull(obj);
//         }

// @Test void smoke_10() {
//             TicketDTO obj = new TicketDTO();
//             assertNotNull(obj);
//         }

// @Test void smoke_11() {
//             TicketDTO obj = new TicketDTO();
//             assertNotNull(obj);
//         }

// @Test void smoke_12() {
//             TicketDTO obj = new TicketDTO();
//             assertNotNull(obj);
//         }

// @Test void smoke_13() {
//             TicketDTO obj = new TicketDTO();
//             assertNotNull(obj);
//         }

// @Test void smoke_14() {
//             TicketDTO obj = new TicketDTO();
//             assertNotNull(obj);
//         }

// @Test void smoke_15() {
//             TicketDTO obj = new TicketDTO();
//             assertNotNull(obj);
//         }
// }
