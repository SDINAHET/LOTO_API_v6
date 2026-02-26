package com.fdjloto.api.controller;

import com.fdjloto.api.model.Historique20Detail;
import com.fdjloto.api.service.Historique20DetailService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

@Controller
public class TirageParDateController {

    private final Historique20DetailService detailService;

    public TirageParDateController(Historique20DetailService detailService) {
        this.detailService = detailService;
    }

    @GetMapping("/tirage/{date}")
    public String tirageParDate(@PathVariable String date, Model model) {

        LocalDate ld;
        try {
            ld = LocalDate.parse(date); // yyyy-MM-dd
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format attendu : yyyy-MM-dd");
        }

        DayOfWeek day = ld.getDayOfWeek();
        if (!(day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY || day == DayOfWeek.SATURDAY)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Optional<Historique20Detail> detailsOpt = detailService.getTirageByDate(date);
        if (detailsOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Historique20Detail details = detailsOpt.get();

        Optional<Historique20Detail> prev = detailService.getTiragePrecedent(ld);
        Optional<Historique20Detail> next = detailService.getTirageSuivant(ld);

        // ✅ Liens ISO prêts pour Thymeleaf (fiable, simple)
        String prevIso = prev.map(p -> toIsoDate(p.getDateDeTirage())).orElse(null);
        String nextIso = next.map(n -> toIsoDate(n.getDateDeTirage())).orElse(null);

        model.addAttribute("prev", prev.orElse(null));
        model.addAttribute("next", next.orElse(null));
        model.addAttribute("prevIso", prevIso);
        model.addAttribute("nextIso", nextIso);

        String dateFr = ld.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH));

        ZoneId paris = ZoneId.of("Europe/Paris");
        String startDateIso = ld.atTime(20, 0)
                .atZone(paris)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        model.addAttribute("details", details);
        model.addAttribute("dateFr", dateFr);
        model.addAttribute("dateIso", date);
        model.addAttribute("startDateIso", startDateIso);

        // (optionnel) si tu veux les utiliser dans le <head>
        model.addAttribute("seoTitle", "Résultat Loto du " + dateFr + " | Loto Tracker");
        model.addAttribute("seoDescription",
                "Résultat officiel du Loto du " + dateFr + " : numéros gagnants, numéro Chance et jackpot.");

        return "tirage-date";
    }

    // ✅ Helper manquant : Date -> yyyy-MM-dd en Europe/Paris
    private String toIsoDate(Date date) {
        if (date == null) return null;
        ZoneId paris = ZoneId.of("Europe/Paris");
        return date.toInstant().atZone(paris).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
