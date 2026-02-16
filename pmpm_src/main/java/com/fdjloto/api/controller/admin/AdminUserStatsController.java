package com.fdjloto.api.controller.admin;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserStatsController {

    private final JdbcTemplate jdbc;

    public AdminUserStatsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record UserStatsView(
            String id,
            String email,
            String firstName,
            String lastName,
            boolean admin,
            int ticketsCount,
            BigDecimal totalGain,
            BigDecimal bestGain,
            Instant lastTicketDate
    ) {}

    @GetMapping("/users-stats")
    public List<UserStatsView> getUsersStats() {

        // ✅ Ajuste les noms de colonnes si besoin (first_name, last_name, created_at, etc.)
        String sql = """
            SELECT
              u.id                                   AS id,
              u.email                                AS email,
              u.first_name                           AS first_name,
              u.last_name                            AS last_name,
              COALESCE(u.admin, false)               AS admin,
              COALESCE(COUNT(t.id), 0)               AS tickets_count,
              COALESCE(SUM(tg.gain_amount), 0)       AS total_gain,
              COALESCE(MAX(tg.gain_amount), 0)       AS best_gain,
              MAX(t.created_at)                      AS last_ticket_date
            FROM users u
            LEFT JOIN tickets t ON t.user_id = u.id
            LEFT JOIN ticket_gains tg ON tg.ticket_id = t.id
            GROUP BY u.id, u.email, u.first_name, u.last_name, u.admin
            """;

        return jdbc.query(sql, (rs, i) -> {
            Timestamp last = rs.getTimestamp("last_ticket_date");
            return new UserStatsView(
                    rs.getString("id"),
                    rs.getString("email"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getBoolean("admin"),
                    rs.getInt("tickets_count"),
                    rs.getBigDecimal("total_gain"),
                    rs.getBigDecimal("best_gain"),
                    last == null ? null : last.toInstant()
            );
        });
    }
}
