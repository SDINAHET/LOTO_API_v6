// package com.fdjloto.api.controller;

// import com.fdjloto.api.model.Historique20Detail;
// import com.fdjloto.api.service.Historique20DetailService;
// import org.springframework.http.HttpStatus;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.server.ResponseStatusException;

// import java.time.*;
// import java.time.format.DateTimeFormatter;
// import java.util.Date;
// import java.util.Locale;
// import java.util.Optional;
// // import java.time.ZoneOffset;

// @Controller
// public class TirageParDateController {

//     private final Historique20DetailService detailService;

//     public TirageParDateController(Historique20DetailService detailService) {
//         this.detailService = detailService;
//     }

//     @GetMapping("/tirage/{date}")
//     public String tirageParDate(@PathVariable String date, Model model) {

//         LocalDate ld;
//         try {
//             ld = LocalDate.parse(date); // yyyy-MM-dd
//         } catch (Exception e) {
//             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format attendu : yyyy-MM-dd");
//         }

//         DayOfWeek day = ld.getDayOfWeek();
//         if (!(day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY || day == DayOfWeek.SATURDAY)) {
//             throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//         }

//         Optional<Historique20Detail> detailsOpt = detailService.getTirageByDate(date);
//         if (detailsOpt.isEmpty()) {
//             throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//         }

//         Historique20Detail details = detailsOpt.get();

//         Optional<Historique20Detail> prev = detailService.getTiragePrecedent(ld);
//         Optional<Historique20Detail> next = detailService.getTirageSuivant(ld);

//         // ✅ Liens ISO prêts pour Thymeleaf (fiable, simple)
//         String prevIso = prev.map(p -> toIsoDate(p.getDateDeTirage())).orElse(null);
//         String nextIso = next.map(n -> toIsoDate(n.getDateDeTirage())).orElse(null);

//         model.addAttribute("prev", prev.orElse(null));
//         model.addAttribute("next", next.orElse(null));
//         model.addAttribute("prevIso", prevIso);
//         model.addAttribute("nextIso", nextIso);

//         String dateFr = ld.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH));

//         ZoneId paris = ZoneId.of("Europe/Paris");
//         String startDateIso = ld.atTime(20, 0)
//                 .atZone(paris)
//                 .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

//         model.addAttribute("details", details);
//         model.addAttribute("dateFr", dateFr);
//         model.addAttribute("dateIso", date);
//         model.addAttribute("startDateIso", startDateIso);
//         model.addAttribute("pageUrl", "https://loto-tracker.fr/tirage/" + date);

//         // (optionnel) si tu veux les utiliser dans le <head>
//         model.addAttribute("seoTitle", "Résultat Loto du " + dateFr + " | Loto Tracker");
//         model.addAttribute("seoDescription",
//                 "Résultat officiel du Loto du " + dateFr + " : numéros gagnants, numéro Chance et jackpot.");

//         return "tirage-date";
//     }

//     // ✅ Helper manquant : Date -> yyyy-MM-dd en Europe/Paris
//     private String toIsoDate(Date date) {
//         if (date == null) return null;
//         ZoneId paris = ZoneId.of("Europe/Paris");
//         return date.toInstant().atZone(paris).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
//     }
// }

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


@GetMapping("/resultat-loto-{date}")
public String resultatLotoSeo(@PathVariable String date, Model model) {
    // ex: /resultat-loto-2026-03-04
    return tirageParDate(date, model);
}

@GetMapping("/tirage-loto-{jour}-{date}")
public String tirageLotoJourSeo(
        @PathVariable String jour,
        @PathVariable String date,
        Model model) {

    // ex: /tirage-loto-samedi-2026-03-07
    // On accepte aussi sans vérifier le slug "jour" pour éviter les 404 inutiles SEO
    // (ou tu peux le valider si tu veux être strict)
    return tirageParDate(date, model);
}

@GetMapping("/resultat-loto-aujourdhui")
public String resultatLotoAujourdhui(Model model) {
    LocalDate d = effectiveTodayForDraws();
    LocalDate draw = nearestDrawOnOrAfter(d);
    return tirageParDate(draw.toString(), model);
}

@GetMapping("/resultat-loto-hier")
public String resultatLotoHier(Model model) {
    LocalDate d = effectiveTodayForDraws().minusDays(1);
    LocalDate draw = nearestDrawOnOrBefore(d);
    return tirageParDate(draw.toString(), model);
}

@GetMapping("/prochain-tirage-loto")
public String prochainTirage(Model model) {
    LocalDate d = effectiveTodayForDraws();
    LocalDate draw = nearestDrawOnOrAfter(d);
    return tirageParDate(draw.toString(), model);
}


@GetMapping("/tirage/{date}")
public String tirageParDate(@PathVariable String date, Model model) {

    ZoneId paris = ZoneId.of("Europe/Paris");

    // 1) Valider format
    LocalDate ld;
    try {
        ld = LocalDate.parse(date); // yyyy-MM-dd
    } catch (Exception e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format attendu : yyyy-MM-dd");
    }

    // 2) Autoriser uniquement Lundi/Mercredi/Samedi
    DayOfWeek day = ld.getDayOfWeek();
    boolean isDrawDay = (day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY || day == DayOfWeek.SATURDAY);
    if (!isDrawDay) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    // 3) Chercher en base
    Optional<Historique20Detail> detailsOpt = detailService.getTirageByDate(date);

    // Helpers prev/next (calendrier pur, marche même sans DB)
    String prevIso = previousDrawDay(ld).toString();
    String nextIso = nextDrawDay(ld).toString();

    // Formats affichage
    String dateFr = ld.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH));
    dateFr = dateFr.substring(0, 1).toUpperCase(Locale.FRENCH) + dateFr.substring(1);
    String startDateIso = ld.atTime(20, 0).atZone(paris).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    String pageUrl = "https://loto-tracker.fr/tirage/" + date;

    // ✅ Date SEO courte (sans le jour)
    String dateFrSeo = ld.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH));

    // ✅ Event status prêt pour le JSON-LD
    String eventStatusUrl = detailsOpt.isEmpty()
            ? "https://schema.org/EventScheduled"
            : "https://schema.org/EventCompleted";

    // 4) Cas FUTUR / pas en base => 200 + page "en attente"
    if (detailsOpt.isEmpty()) {
        model.addAttribute("details", null);
        model.addAttribute("isPending", true);

        model.addAttribute("dateFr", dateFr);
        model.addAttribute("dateFrSeo", dateFrSeo);          // ✅ AJOUT
        model.addAttribute("eventStatusUrl", eventStatusUrl); // ✅ AJOUT

        model.addAttribute("dateIso", date);
        model.addAttribute("startDateIso", startDateIso);
        model.addAttribute("pageUrl", pageUrl);

        model.addAttribute("prevIso", prevIso);
        model.addAttribute("nextIso", nextIso);

        model.addAttribute("seoTitle", "Résultat Loto (FDJ) : tirage du " + dateFr + " | Loto Tracker");
        model.addAttribute("seoDescription",
                "Résultat du Loto du " + dateFr + " : tirage prévu à 20h35. Les numéros gagnants et rapports seront publiés dès l’annonce officielle.");

        return "tirage-date"; // ✅ HTTP 200
    }

    // 5) Cas OK (en base)
    Historique20Detail details = detailsOpt.get();
    model.addAttribute("details", details);
    model.addAttribute("isPending", false);

    model.addAttribute("dateFr", dateFr);
    model.addAttribute("dateFrSeo", dateFrSeo);           // ✅ AJOUT
    model.addAttribute("eventStatusUrl", eventStatusUrl); // ✅ AJOUT
    model.addAttribute("dateIso", date);
    model.addAttribute("startDateIso", startDateIso);
    model.addAttribute("pageUrl", pageUrl);

    // (optionnel) tu peux garder ton prev/next DB si tu préfères,
    // mais ces dates "calendrier" fonctionnent toujours.
    model.addAttribute("prevIso", prevIso);
    model.addAttribute("nextIso", nextIso);

    model.addAttribute("seoTitle", "Résultat Loto (FDJ) : tirage du " + dateFr + " | Loto Tracker");
    model.addAttribute("seoDescription",
            "Résultat officiel du Loto du " + dateFr + " : numéros gagnants, numéro Chance et jackpot.");

    return "tirage-date";
}

private LocalDate nextDrawDay(LocalDate d) {
    LocalDate x = d.plusDays(1);
    while (x.getDayOfWeek() != DayOfWeek.MONDAY
            && x.getDayOfWeek() != DayOfWeek.WEDNESDAY
            && x.getDayOfWeek() != DayOfWeek.SATURDAY) {
        x = x.plusDays(1);
    }
    return x;
}

private LocalDate previousDrawDay(LocalDate d) {
    LocalDate x = d.minusDays(1);
    while (x.getDayOfWeek() != DayOfWeek.MONDAY
            && x.getDayOfWeek() != DayOfWeek.WEDNESDAY
            && x.getDayOfWeek() != DayOfWeek.SATURDAY) {
        x = x.minusDays(1);
    }
    return x;
}



private LocalDate effectiveTodayForDraws() {
    // ✅ après 20h35 (heure officielle), on considère que "aujourd'hui" SEO devient demain
    ZoneId paris = ZoneId.of("Europe/Paris");
    LocalDate today = LocalDate.now(paris);
    LocalTime now = LocalTime.now(paris);
    if (now.isAfter(LocalTime.of(20, 35))) {
        return today.plusDays(1);
    }
    return today;
}

private boolean isDrawDay(LocalDate d) {
    DayOfWeek day = d.getDayOfWeek();
    return day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY || day == DayOfWeek.SATURDAY;
}

private LocalDate nearestDrawOnOrAfter(LocalDate start) {
    LocalDate d = start;
    while (!isDrawDay(d)) d = d.plusDays(1);
    return d;
}

private LocalDate nearestDrawOnOrBefore(LocalDate start) {
    LocalDate d = start;
    while (!isDrawDay(d)) d = d.minusDays(1);
    return d;
}

}
