// // package com.fdjloto.api.controller;

// // import com.fdjloto.api.model.Historique20Detail;
// // import com.fdjloto.api.service.Historique20DetailService;
// // import org.springframework.http.HttpStatus;
// // import org.springframework.stereotype.Controller;
// // import org.springframework.ui.Model;
// // import org.springframework.web.bind.annotation.GetMapping;
// // import org.springframework.web.bind.annotation.PathVariable;
// // import org.springframework.web.server.ResponseStatusException;

// // import java.time.*;
// // import java.time.format.DateTimeFormatter;
// // import java.util.Date;
// // import java.util.Locale;
// // import java.util.Optional;
// // // import java.time.ZoneOffset;

// // @Controller
// // public class TirageParDateController {

// //     private final Historique20DetailService detailService;

// //     public TirageParDateController(Historique20DetailService detailService) {
// //         this.detailService = detailService;
// //     }

// //     @GetMapping("/tirage/{date}")
// //     public String tirageParDate(@PathVariable String date, Model model) {

// //         LocalDate ld;
// //         try {
// //             ld = LocalDate.parse(date); // yyyy-MM-dd
// //         } catch (Exception e) {
// //             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format attendu : yyyy-MM-dd");
// //         }

// //         DayOfWeek day = ld.getDayOfWeek();
// //         if (!(day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY || day == DayOfWeek.SATURDAY)) {
// //             throw new ResponseStatusException(HttpStatus.NOT_FOUND);
// //         }

// //         Optional<Historique20Detail> detailsOpt = detailService.getTirageByDate(date);
// //         if (detailsOpt.isEmpty()) {
// //             throw new ResponseStatusException(HttpStatus.NOT_FOUND);
// //         }

// //         Historique20Detail details = detailsOpt.get();

// //         Optional<Historique20Detail> prev = detailService.getTiragePrecedent(ld);
// //         Optional<Historique20Detail> next = detailService.getTirageSuivant(ld);

// //         // ✅ Liens ISO prêts pour Thymeleaf (fiable, simple)
// //         String prevIso = prev.map(p -> toIsoDate(p.getDateDeTirage())).orElse(null);
// //         String nextIso = next.map(n -> toIsoDate(n.getDateDeTirage())).orElse(null);

// //         model.addAttribute("prev", prev.orElse(null));
// //         model.addAttribute("next", next.orElse(null));
// //         model.addAttribute("prevIso", prevIso);
// //         model.addAttribute("nextIso", nextIso);

// //         String dateFr = ld.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH));

// //         ZoneId paris = ZoneId.of("Europe/Paris");
// //         String startDateIso = ld.atTime(20, 0)
// //                 .atZone(paris)
// //                 .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

// //         model.addAttribute("details", details);
// //         model.addAttribute("dateFr", dateFr);
// //         model.addAttribute("dateIso", date);
// //         model.addAttribute("startDateIso", startDateIso);
// //         model.addAttribute("pageUrl", "https://loto-tracker.fr/tirage/" + date);

// //         // (optionnel) si tu veux les utiliser dans le <head>
// //         model.addAttribute("seoTitle", "Résultat Loto du " + dateFr + " | Loto Tracker");
// //         model.addAttribute("seoDescription",
// //                 "Résultat officiel du Loto du " + dateFr + " : numéros gagnants, numéro Chance et jackpot.");

// //         return "tirage-date";
// //     }

// //     // ✅ Helper manquant : Date -> yyyy-MM-dd en Europe/Paris
// //     private String toIsoDate(Date date) {
// //         if (date == null) return null;
// //         ZoneId paris = ZoneId.of("Europe/Paris");
// //         return date.toInstant().atZone(paris).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
// //     }
// // }

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

// @Controller
// public class TirageParDateController {

//     private final Historique20DetailService detailService;

//     public TirageParDateController(Historique20DetailService detailService) {
//         this.detailService = detailService;
//     }

//     @GetMapping("/tirage/{date}")
//     public String tirageParDate(@PathVariable String date, Model model) {

//         // 1) Valider le format URL
//         LocalDate ldFromUrl;
//         try {
//             ldFromUrl = LocalDate.parse(date); // yyyy-MM-dd
//         } catch (Exception e) {
//             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format attendu : yyyy-MM-dd");
//         }

//         // 2) Charger les données (par la date demandée)
//         Optional<Historique20Detail> detailsOpt = detailService.getTirageByDate(date);
//         if (detailsOpt.isEmpty()) {
//             throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//         }

//         Historique20Detail details = detailsOpt.get();
//         if (details.getDateDeTirage() == null) {
//             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Date de tirage manquante");
//         }

//         // 3) Canonicaliser la date depuis la DB en Europe/Paris (évite +1/-1 jour)
//         ZoneId paris = ZoneId.of("Europe/Paris");
//         LocalDate ldDb = details.getDateDeTirage().toInstant().atZone(paris).toLocalDate();
//         String isoDb = ldDb.format(DateTimeFormatter.ISO_LOCAL_DATE);

//         // 4) Vérifier jour autorisé (sur la vraie date DB)
//         DayOfWeek day = ldDb.getDayOfWeek();
//         if (day != DayOfWeek.MONDAY && day != DayOfWeek.WEDNESDAY && day != DayOfWeek.SATURDAY) {
//             throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//         }

//         // 5) Si l’URL ne correspond pas à la date DB -> redirect vers l’URL canonique (SEO + cohérence)
//         if (!isoDb.equals(date)) {
//             return "redirect:/tirage/" + isoDb;
//         }

//         // 6) Prev/Next basés sur la date DB (pas sur l’URL)
//         Optional<Historique20Detail> prev = detailService.getTiragePrecedent(ldDb);
//         Optional<Historique20Detail> next = detailService.getTirageSuivant(ldDb);

//         String prevIso = prev.map(p -> toIsoDate(p.getDateDeTirage(), paris)).orElse(null);
//         String nextIso = next.map(n -> toIsoDate(n.getDateDeTirage(), paris)).orElse(null);

//         // 7) Formats d’affichage
//         // String dateFr = ldDb.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH));
//         String dateFr = ldDb.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH));
//         if (dateFr != null && !dateFr.isEmpty()) {
//             dateFr = dateFr.substring(0, 1).toUpperCase(Locale.FRENCH) + dateFr.substring(1);
//         }
//         // Start date du tirage (20:00 Paris) ISO_OFFSET_DATE_TIME
//         String startDateIso = ldDb.atTime(20, 0).atZone(paris).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

//         // 8) Model
//         model.addAttribute("details", details);
//         model.addAttribute("dateFr", dateFr);
//         model.addAttribute("dateIso", isoDb);
//         model.addAttribute("startDateIso", startDateIso);
//         model.addAttribute("pageUrl", "https://loto-tracker.fr/tirage/" + isoDb);

//         model.addAttribute("prev", prev.orElse(null));
//         model.addAttribute("next", next.orElse(null));
//         model.addAttribute("prevIso", prevIso);
//         model.addAttribute("nextIso", nextIso);

//         // model.addAttribute("seoTitle", "Résultat Loto du " + dateFr + " | Loto Tracker");
//         model.addAttribute("seoTitle", "Résultat Loto (FDJ) : tirage du " + dateFr + " | Loto Tracker");
//         model.addAttribute("seoDescription",
//                 "Résultat officiel du Loto du " + dateFr + " : numéros gagnants, numéro Chance et jackpot.");

//         return "tirage-date";
//     }

//     private String toIsoDate(Date date, ZoneId zone) {
//         if (date == null) return null;
//         return date.toInstant().atZone(zone).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
//     }
// }
