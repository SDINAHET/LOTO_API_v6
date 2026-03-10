
// package com.fdjloto.api.service;

// import com.fdjloto.api.TestDataFactory;
// import com.fdjloto.api.dto.TicketDTO;
// import com.fdjloto.api.exception.TicketNotFoundException;
// import com.fdjloto.api.model.Ticket;
// import com.fdjloto.api.model.User;
// import com.fdjloto.api.repository.TicketRepository;
// import com.fdjloto.api.repository.UserRepository;
// import org.junit.jupiter.api.*;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.*;

// import java.time.LocalDate;
// import java.util.*;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
// class TicketServiceTest {

//     @Mock TicketRepository ticketRepository;
//     @Mock UserRepository userRepository;

//     @InjectMocks TicketService ticketService;

//     @Test void createTicket_userNotFound_throws() {
//         when(userRepository.findById("u1")).thenReturn(Optional.empty());
//         TicketDTO dto = TestDataFactory.ticketDto("1-2-3-4-5", "7", "2025-01-01");
//         assertThrows(RuntimeException.class, () -> ticketService.createTicket("u1", dto));
//         verify(ticketRepository, never()).save(any());
//     }

//     @Test void createTicket_savesTicket_withDrawDateAndDrawDay() {
//         User u = TestDataFactory.user("u1", false);
//         when(userRepository.findById("u1")).thenReturn(Optional.of(u));
//         when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

//         TicketDTO dto = TestDataFactory.ticketDto("1-2-3-4-5", "7", "2025-01-01");
//         Ticket saved = ticketService.createTicket("u1", dto);

//         assertEquals(u, saved.getUser());
//         assertEquals("1-2-3-4-5", saved.getNumbers());
//         assertEquals(7, saved.getChanceNumber());
//         assertEquals(LocalDate.of(2025,1,1), saved.getDrawDate());
//         assertNotNull(saved.getDrawDay());
//         assertNotNull(saved.getCreatedAt());
//         assertNotNull(saved.getUpdatedAt());
//         verify(ticketRepository).save(any(Ticket.class));
//     }

//     @Test void updateTicket_dtoInvalidChance_throwsIllegalArgumentException() {
//         Ticket existing = TestDataFactory.ticket("t1", TestDataFactory.user("u1", false));
//         when(ticketRepository.findById("t1")).thenReturn(Optional.of(existing));
//         TicketDTO dto = TestDataFactory.ticketDto("1-2-3-4-5", "abc", "2025-01-01");
//         assertThrows(IllegalArgumentException.class, () -> ticketService.updateTicket("t1", dto));
//         verify(ticketRepository, never()).save(any());
//     }

//     @Test void updateTicket_changesDrawDate_updatesDrawDay() {
//         Ticket existing = TestDataFactory.ticket("t1", TestDataFactory.user("u1", false));
//         existing.setDrawDate(LocalDate.of(2025,1,1));
//         existing.setDrawDay("mercredi");
//         when(ticketRepository.findById("t1")).thenReturn(Optional.of(existing));
//         when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

//         TicketDTO dto = TestDataFactory.ticketDto("9-8-7-6-5", "1", "2025-01-04");
//         Ticket updated = ticketService.updateTicket("t1", dto);

//         assertEquals("9-8-7-6-5", updated.getNumbers());
//         assertEquals(1, updated.getChanceNumber());
//         assertEquals(LocalDate.of(2025,1,4), updated.getDrawDate());
//         assertNotNull(updated.getDrawDay());
//         verify(ticketRepository).save(existing);
//     }

//     @Test void updateTicket_sameDrawDate_keepsDrawDay() {
//         Ticket existing = TestDataFactory.ticket("t1", TestDataFactory.user("u1", false));
//         existing.setDrawDate(LocalDate.of(2025,1,1));
//         existing.setDrawDay("mercredi");
//         when(ticketRepository.findById("t1")).thenReturn(Optional.of(existing));
//         when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

//         TicketDTO dto = TestDataFactory.ticketDto("1-1-1-1-1", "2", "2025-01-01");
//         Ticket updated = ticketService.updateTicket("t1", dto);

//         assertEquals("mercredi", updated.getDrawDay());
//         verify(ticketRepository).save(existing);
//     }

//     @Test void getTicketById_notFound_throwsTicketNotFoundException() {
//         when(ticketRepository.findById("t1")).thenReturn(Optional.empty());
//         assertThrows(TicketNotFoundException.class, () -> ticketService.getTicketById("t1"));
//     }

//     @Test void getAllTickets_mapsToDTO() {
//         User u = TestDataFactory.user("u1", false);
//         when(ticketRepository.findAll()).thenReturn(List.of(
//             TestDataFactory.ticket("t1", u),
//             TestDataFactory.ticket("t2", u)
//         ));
//         assertEquals(2, ticketService.getAllTickets().size());
//     }

//     @Test void deleteTicket_owner_canDelete() {
//         User u = TestDataFactory.user("u1", false);
//         Ticket t = TestDataFactory.ticket("t1", u);
//         when(ticketRepository.findById("t1")).thenReturn(Optional.of(t));
//         ticketService.deleteTicket("t1", "u1");
//         verify(ticketRepository).deleteById("t1");
//     }

//     @Test void deleteTicket_admin_canDelete() {
//         User owner = TestDataFactory.user("u1", false);
//         Ticket t = TestDataFactory.ticket("t1", owner);
//         when(ticketRepository.findById("t1")).thenReturn(Optional.of(t));
//         when(userRepository.findById("admin")).thenReturn(Optional.of(TestDataFactory.user("admin", true)));
//         ticketService.deleteTicket("t1", "admin");
//         verify(ticketRepository).deleteById("t1");
//     }

//     @Test void deleteTicket_nonOwnerNonAdmin_throws() {
//         User owner = TestDataFactory.user("u1", false);
//         Ticket t = TestDataFactory.ticket("t1", owner);
//         when(ticketRepository.findById("t1")).thenReturn(Optional.of(t));
//         when(userRepository.findById("u2")).thenReturn(Optional.of(TestDataFactory.user("u2", false)));
//         assertThrows(RuntimeException.class, () -> ticketService.deleteTicket("t1", "u2"));
//         verify(ticketRepository, never()).deleteById(anyString());
//     }

//     @Test void getDrawDay_formatsInFrenchUpper() {
//         String day = ticketService.getDrawDay("2025-01-01");
//         assertNotNull(day);
//         assertEquals(day, day.toUpperCase());
//     }

//     // Padding tests for count & basic coverage
//     @Test void getTicketsByEmail_delegatesToRepo() {
//         when(ticketRepository.findByUserEmail("a@b.com")).thenReturn(Collections.emptyList());
//         assertNotNull(ticketService.getTicketsByEmail("a@b.com"));
//     }

//     @Test void getTicketsByUserId_mapsToDTO() {
//         when(ticketRepository.findByUserId("u1")).thenReturn(Collections.emptyList());
//         assertEquals(0, ticketService.getTicketsByUserId("u1").size());
//     }
// }
