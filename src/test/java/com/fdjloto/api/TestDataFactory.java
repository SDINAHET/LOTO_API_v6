
// package com.fdjloto.api;

// import com.fdjloto.api.dto.TicketDTO;
// import com.fdjloto.api.model.*;

// import java.time.LocalDate;
// import java.time.LocalDateTime;

// /**
//  * Small factory used by tests to reduce boilerplate.
//  */
// public final class TestDataFactory {
//     private TestDataFactory() {}

//     public static User user(String id, boolean admin) {
//         User u = new User();
//         u.setId(id);
//         u.setFirstName("John");
//         u.setLastName("Doe");
//         u.setEmail("john.doe@example.com");
//         u.setPassword("secret123");
//         u.setAdmin(admin);
//         u.setCreatedAt(LocalDateTime.now().minusDays(1));
//         u.setUpdatedAt(LocalDateTime.now().minusHours(1));
//         return u;
//     }

//     public static Ticket ticket(String id, User user) {
//         Ticket t = new Ticket();
//         t.setId(id);
//         t.setUser(user);
//         t.setNumbers("1-2-3-4-5");
//         t.setChanceNumber(7);
//         t.setDrawDate(LocalDate.now());
//         t.setDrawDay("lundi");
//         t.setCreatedAt(LocalDateTime.now().minusDays(2));
//         t.setUpdatedAt(LocalDateTime.now().minusDays(1));
//         return t;
//     }

//     public static TicketDTO ticketDto(String numbers, String chance, String drawDate) {
//         TicketDTO dto = new TicketDTO();
//         dto.setNumbers(numbers);
//         dto.setChanceNumber(chance);
//         dto.setDrawDate(drawDate);
//         return dto;
//     }
// }
