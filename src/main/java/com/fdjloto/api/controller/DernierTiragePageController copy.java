// package com.fdjloto.api.controller;

// import com.fdjloto.api.dto.LotoResultDTO;
// import com.fdjloto.api.model.Historique20Detail;
// import com.fdjloto.api.model.Historique20Result;
// import com.fdjloto.api.service.Historique20DetailService;
// import com.fdjloto.api.service.Historique20Service;
// import org.springframework.http.HttpStatus;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.server.ResponseStatusException;

// import java.time.*;
// import java.time.format.DateTimeFormatter;
// import java.util.List;
// import java.util.Locale;
// import java.util.Optional;

// @Controller
// public class DernierTiragePageController {

//     private final Historique20Service historique20Service;
//     private final Historique20DetailService detailService;

//     public DernierTiragePageController(Historique20Service historique20Service,
//                                        Historique20DetailService detailService) {
//         this.historique20Service = historique20Service;
//         this.detailService = detailService;
//     }

//     @GetMapping({"/dernier-tirage", "/dernier-tirage/"})
//     public String dernierTirage(Model model) {

//         List<Historique20Result> results = historique20Service.getLast20Results();
//         if (results == null || results.isEmpty()) {
//             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun tirage disponible");
//         }

//         // 1er résultat = dernier tirage
//         Historique20Result r = results.get(0);
//         if (r.getDateDeTirage() == null) {
//             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Date de tirage manquante");
//         }

//         // mapping identique à ton API JSON
//         LotoResultDTO tirage = new LotoResultDTO(
//                 r.getId(),
//                 r.getDateDeTirage(),
//                 r.getBoule1(),
//                 r.getBoule2(),
//                 r.getBoule3(),
//                 r.getBoule4(),
//                 r.getBoule5(),
//                 r.getNumeroChance()
//         );

//         ZoneId paris = ZoneId.of("Europe/Paris");

//         LocalDate ld = tirage.getDateDeTirage().toInstant()
//                 .atZone(paris)
//                 .toLocalDate();

//         String dateIso = ld.format(DateTimeFormatter.ISO_LOCAL_DATE);

//         Optional<Historique20Detail> detailsOpt = detailService.getTirageByDate(dateIso);

//         String dateFr = ld.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH));

//         String nums = tirage.getBoule1() + " " + tirage.getBoule2() + " " + tirage.getBoule3()
//                 + " " + tirage.getBoule4() + " " + tirage.getBoule5();

//         // ISO_OFFSET_DATE_TIME correct (heure été/hiver gérée)
//         ZonedDateTime startParis = ld.atTime(20, 0).atZone(paris);
//         String startDateIso = startParis.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

//         String seoTitle = "Dernier Résultat du Loto Français (" + dateFr + ") | Loto Tracker";
//         String seoDescription = "Résultat du " + dateFr + " : " + nums + " - Numéro Chance " + tirage.getNumeroChance()
//                 + ". Consultez les détails du tirage et l'historique.";

//         model.addAttribute("tirage", tirage);
//         model.addAttribute("details", detailsOpt.orElse(null));
//         model.addAttribute("dateFr", dateFr);
//         model.addAttribute("nums", nums);
//         model.addAttribute("startDateIso", startDateIso);
//         model.addAttribute("seoTitle", seoTitle);
//         model.addAttribute("seoDescription", seoDescription);

//         return "dernier-tirage";
//     }
// }
