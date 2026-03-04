// // // package com.fdjloto.api.controller;

// // // import org.springframework.http.MediaType;
// // // import org.springframework.stereotype.Controller;
// // // import org.springframework.web.bind.annotation.GetMapping;
// // // import org.springframework.web.bind.annotation.ResponseBody;

// // // @Controller
// // // public class SitemapController {

// // //     @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
// // //     @ResponseBody
// // //     public String sitemap() {

// // //         String baseUrl = "https://loto-tracker.fr";

// // //         return """
// // //             <?xml version="1.0" encoding="UTF-8"?>
// // //             <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
// // //               <url>
// // //                 <loc>%s/</loc>
// // //                 <changefreq>daily</changefreq>
// // //                 <priority>1.0</priority>
// // //               </url>
// // //               <url>
// // //                 <loc>%s/dernier-tirage</loc>
// // //                 <changefreq>daily</changefreq>
// // //                 <priority>0.9</priority>
// // //               </url>
// // //             </urlset>
// // //             """.formatted(baseUrl, baseUrl);
// // //     }
// // // }

// // // package com.fdjloto.api.controller;

// // // import com.fdjloto.api.model.Historique20Detail;
// // // import com.fdjloto.api.service.Historique20DetailService;
// // // import org.springframework.http.MediaType;
// // // import org.springframework.stereotype.Controller;
// // // import org.springframework.web.bind.annotation.GetMapping;
// // // import org.springframework.web.bind.annotation.ResponseBody;

// // // import java.time.DayOfWeek;
// // // import java.time.LocalDate;
// // // import java.time.ZoneId;
// // // import java.util.List;

// // // @Controller
// // // public class SitemapController {

// // //     private static final String BASE_URL = "https://loto-tracker.fr";
// // //     private final Historique20DetailService detailService;

// // //     public SitemapController(Historique20DetailService detailService) {
// // //         this.detailService = detailService;
// // //     }

// // //     /**
// // //      * ✅ Sitemap index (celui à déclarer dans Google Search Console)
// // //      */
// // //     @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
// // //     @ResponseBody
// // //     public String sitemapIndex() {
// // //         return """
// // //             <?xml version="1.0" encoding="UTF-8"?>
// // //             <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
// // //               <sitemap>
// // //                 <loc>%s/sitemap-static.xml</loc>
// // //               </sitemap>
// // //               <sitemap>
// // //                 <loc>%s/sitemap-tirages.xml</loc>
// // //               </sitemap>
// // //             </sitemapindex>
// // //             """.formatted(BASE_URL, BASE_URL);
// // //     }

// // //     /**
// // //      * ✅ Pages fixes
// // //      */
// // //     @GetMapping(value = "/sitemap-static.xml", produces = MediaType.APPLICATION_XML_VALUE)
// // //     @ResponseBody
// // //     public String sitemapStatic() {
// // //         return """
// // //             <?xml version="1.0" encoding="UTF-8"?>
// // //             <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
// // //               <url>
// // //                 <loc>%s/</loc>
// // //                 <changefreq>daily</changefreq>
// // //                 <priority>1.0</priority>
// // //               </url>
// // //               <url>
// // //                 <loc>%s/dernier-tirage</loc>
// // //                 <changefreq>daily</changefreq>
// // //                 <priority>0.9</priority>
// // //               </url>
// // //             </urlset>
// // //             """.formatted(BASE_URL, BASE_URL);
// // //     }

// // //     /**
// // //      * ✅ Pages SEO par date : /tirage/yyyy-MM-dd
// // //      */
// // // 	@GetMapping(value = "/sitemap-tirages.xml", produces = MediaType.APPLICATION_XML_VALUE)
// // // 	@ResponseBody
// // // 	public String sitemapTirages() {

// // // 		List<Historique20Detail> tirages = detailService.getAllTirages();

// // // 		StringBuilder sb = new StringBuilder(512);
// // // 		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
// // // 		sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

// // // 		ZoneId paris = ZoneId.of("Europe/Paris");
// // // 		LocalDate today = LocalDate.now(paris);

// // // 		for (Historique20Detail t : tirages) {

// // // 			if (t.getDateDeTirage() == null) continue;

// // // 			LocalDate ld = t.getDateDeTirage()
// // // 					.toInstant()
// // // 					.atZone(paris)
// // // 					.toLocalDate();

// // // 			DayOfWeek day = ld.getDayOfWeek();

// // // 			// 🔥 Autoriser uniquement Lundi / Mercredi / Samedi
// // // 			if (!(day == DayOfWeek.MONDAY
// // // 					|| day == DayOfWeek.WEDNESDAY
// // // 					|| day == DayOfWeek.SATURDAY)) {
// // // 				continue;
// // // 			}

// // // 			String iso = ld.toString();

// // // 			// 🔥 Priorité dynamique : plus récent = plus important
// // // 			long daysOld = java.time.temporal.ChronoUnit.DAYS.between(ld, today);

// // // 			String priority;
// // // 			if (daysOld <= 7) {
// // // 				priority = "0.9";
// // // 			} else if (daysOld <= 30) {
// // // 				priority = "0.8";
// // // 			} else {
// // // 				priority = "0.6";
// // // 			}

// // // 			sb.append("<url>");
// // // 			sb.append("<loc>").append(BASE_URL).append("/tirage/").append(iso).append("</loc>");
// // // 			sb.append("<lastmod>").append(iso).append("</lastmod>");
// // // 			sb.append("<changefreq>yearly</changefreq>");
// // // 			sb.append("<priority>").append(priority).append("</priority>");
// // // 			sb.append("</url>");
// // // 		}

// // // 		sb.append("</urlset>");
// // // 		return sb.toString();
// // // 	}
// // // }

// // // package com.fdjloto.api.controller;

// // // import com.fdjloto.api.model.Historique20Detail;
// // // import com.fdjloto.api.service.Historique20DetailService;
// // // import org.springframework.http.MediaType;
// // // import org.springframework.stereotype.Controller;
// // // import org.springframework.web.bind.annotation.GetMapping;
// // // import org.springframework.web.bind.annotation.ResponseBody;

// // // import java.time.DayOfWeek;
// // // import java.time.LocalDate;
// // // import java.time.ZoneId;
// // // import java.time.temporal.ChronoUnit;
// // // import java.util.Comparator;
// // // import java.util.List;

// // // @Controller
// // // public class SitemapController {

// // //     private static final String BASE_URL = "https://loto-tracker.fr";
// // //     private final Historique20DetailService detailService;

// // //     public SitemapController(Historique20DetailService detailService) {
// // //         this.detailService = detailService;
// // //     }

// // //     /**
// // //      * ✅ Sitemap index (celui à déclarer dans Google Search Console)
// // //      */
// // //     @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
// // //     @ResponseBody
// // //     public String sitemapIndex() {

// // //         ZoneId paris = ZoneId.of("Europe/Paris");
// // //         String lastmod = LocalDate.now(paris).toString();

// // //         return """
// // //             <?xml version="1.0" encoding="UTF-8"?>
// // //             <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
// // //               <sitemap>
// // //                 <loc>%s/sitemap-static.xml</loc>
// // //                 <lastmod>%s</lastmod>
// // //               </sitemap>
// // //               <sitemap>
// // //                 <loc>%s/sitemap-tirages.xml</loc>
// // //                 <lastmod>%s</lastmod>
// // //               </sitemap>
// // //             </sitemapindex>
// // //             """.formatted(BASE_URL, lastmod, BASE_URL, lastmod);
// // //     }

// // //     /**
// // //      * ✅ Pages fixes
// // //      */
// // //     @GetMapping(value = "/sitemap-static.xml", produces = MediaType.APPLICATION_XML_VALUE)
// // //     @ResponseBody
// // //     public String sitemapStatic() {

// // //         ZoneId paris = ZoneId.of("Europe/Paris");
// // //         String lastmod = LocalDate.now(paris).toString();

// // //         return """
// // //             <?xml version="1.0" encoding="UTF-8"?>
// // //             <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
// // //               <url>
// // //                 <loc>%s/</loc>
// // //                 <lastmod>%s</lastmod>
// // //                 <changefreq>daily</changefreq>
// // //                 <priority>1.0</priority>
// // //               </url>
// // //               <url>
// // //                 <loc>%s/dernier-tirage</loc>
// // //                 <lastmod>%s</lastmod>
// // //                 <changefreq>daily</changefreq>
// // //                 <priority>0.9</priority>
// // //               </url>
// // //             </urlset>
// // //             """.formatted(BASE_URL, lastmod, BASE_URL, lastmod);
// // //     }

// // //     /**
// // //      * ✅ Pages SEO par date : /tirage/yyyy-MM-dd
// // //      */
// // //     @GetMapping(value = "/sitemap-tirages.xml", produces = MediaType.APPLICATION_XML_VALUE)
// // //     @ResponseBody
// // //     public String sitemapTirages() {

// // //         List<Historique20Detail> tirages = detailService.getAllTirages();

// // //         ZoneId paris = ZoneId.of("Europe/Paris");
// // //         LocalDate today = LocalDate.now(paris);

// // //         // ✅ Trier du plus récent au plus ancien (meilleur crawl)
// // //         tirages.sort(Comparator.comparing(Historique20Detail::getDateDeTirage,
// // //                 Comparator.nullsLast(Comparator.naturalOrder())).reversed());

// // //         StringBuilder sb = new StringBuilder(16_384);
// // //         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
// // //         sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

// // //         for (Historique20Detail t : tirages) {

// // //             if (t.getDateDeTirage() == null) continue;

// // //             LocalDate ld = t.getDateDeTirage()
// // //                     .toInstant()
// // //                     .atZone(paris)
// // //                     .toLocalDate();

// // //             DayOfWeek day = ld.getDayOfWeek();

// // //             // ✅ Autoriser uniquement Lundi / Mercredi / Samedi
// // //             if (day != DayOfWeek.MONDAY && day != DayOfWeek.WEDNESDAY && day != DayOfWeek.SATURDAY) {
// // //                 continue;
// // //             }

// // //             String iso = ld.toString();

// // //             long daysOld = ChronoUnit.DAYS.between(ld, today);

// // //             // ✅ Priorité dynamique : plus récent = plus important
// // //             String priority;
// // //             if (daysOld <= 7) {
// // //                 priority = "0.9";
// // //             } else if (daysOld <= 30) {
// // //                 priority = "0.8";
// // //             } else if (daysOld <= 365) {
// // //                 priority = "0.7";
// // //             } else {
// // //                 priority = "0.6";
// // //             }

// // //             sb.append("<url>");
// // //             sb.append("<loc>").append(BASE_URL).append("/tirage/").append(iso).append("</loc>");
// // //             sb.append("<lastmod>").append(iso).append("</lastmod>");
// // //             sb.append("<changefreq>never</changefreq>");
// // //             sb.append("<priority>").append(priority).append("</priority>");
// // //             sb.append("</url>");
// // //         }

// // //         sb.append("</urlset>");
// // //         return sb.toString();
// // //     }
// // // }

// // package com.fdjloto.api.controller;

// // import com.fdjloto.api.model.Historique20Detail;
// // import com.fdjloto.api.service.Historique20DetailService;
// // import org.springframework.http.MediaType;
// // import org.springframework.stereotype.Controller;
// // import org.springframework.web.bind.annotation.GetMapping;
// // import org.springframework.web.bind.annotation.ResponseBody;

// // import java.time.DayOfWeek;
// // import java.time.LocalDate;
// // import java.time.ZoneId;
// // import java.time.temporal.ChronoUnit;
// // import java.util.Comparator;
// // import java.util.List;

// // @Controller
// // public class SitemapController {

// //     private static final String BASE_URL = "https://loto-tracker.fr";
// //     private final Historique20DetailService detailService;

// //     public SitemapController(Historique20DetailService detailService) {
// //         this.detailService = detailService;
// //     }

// //     /**
// //      * ✅ Sitemap index (à soumettre dans Google Search Console)
// //      */
// //     @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
// //     @ResponseBody
// //     public String sitemapIndex() {

// //         ZoneId paris = ZoneId.of("Europe/Paris");
// //         String lastmod = LocalDate.now(paris).toString();

// //         return """
// //             <?xml version="1.0" encoding="UTF-8"?>
// //             <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
// //               <sitemap>
// //                 <loc>%s/sitemap-pages.xml</loc>
// //                 <lastmod>%s</lastmod>
// //               </sitemap>
// //               <sitemap>
// //                 <loc>%s/sitemap-tirages.xml</loc>
// //                 <lastmod>%s</lastmod>
// //               </sitemap>
// //               <sitemap>
// //                 <loc>%s/sitemap-static.xml</loc>
// //                 <lastmod>%s</lastmod>
// //               </sitemap>
// //             </sitemapindex>
// //             """.formatted(BASE_URL, lastmod, BASE_URL, lastmod, BASE_URL, lastmod);
// //     }

// //     /**
// //      * ✅ Pages principales (site navigation / SEO)
// //      * Mets ici les pages importantes de ton site (hors pages "légales")
// //      */
// //     @GetMapping(value = "/sitemap-pages.xml", produces = MediaType.APPLICATION_XML_VALUE)
// //     @ResponseBody
// //     public String sitemapPages() {

// //         ZoneId paris = ZoneId.of("Europe/Paris");
// //         String lastmod = LocalDate.now(paris).toString();

// //         // ✅ Ajoute ici toutes les pages "business" importantes
// //         // (exemples) : /historique, /stats, /verifier-ticket, etc.
// //         return """
// //             <?xml version="1.0" encoding="UTF-8"?>
// //             <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
// //               <url>
// //                 <loc>%s/</loc>
// //                 <lastmod>%s</lastmod>
// //                 <changefreq>daily</changefreq>
// //                 <priority>1.0</priority>
// //               </url>
// //               <url>
// //                 <loc>%s/dernier-tirage</loc>
// //                 <lastmod>%s</lastmod>
// //                 <changefreq>daily</changefreq>
// //                 <priority>0.9</priority>
// //               </url>
// //             </urlset>
// //             """.formatted(BASE_URL, lastmod, BASE_URL, lastmod);
// //     }

// //     /**
// //      * ✅ Pages statiques (légales / info)
// //      * Mets ici tes pages fixes type politique de confidentialité, CGU, etc.
// //      */
// //     @GetMapping(value = "/sitemap-static.xml", produces = MediaType.APPLICATION_XML_VALUE)
// //     @ResponseBody
// //     public String sitemapStatic() {

// //         ZoneId paris = ZoneId.of("Europe/Paris");
// //         String lastmod = LocalDate.now(paris).toString();

// //         return """
// //             <?xml version="1.0" encoding="UTF-8"?>
// //             <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
// //               <url>
// //                 <loc>%s/politique_confidentialite.html</loc>
// //                 <lastmod>%s</lastmod>
// //                 <changefreq>yearly</changefreq>
// //                 <priority>0.3</priority>
// //               </url>
// //             </urlset>
// //             """.formatted(BASE_URL, lastmod);
// //     }

// //     /**
// //      * ✅ Pages SEO par date : /tirage/yyyy-MM-dd
// //      */
// //     @GetMapping(value = "/sitemap-tirages.xml", produces = MediaType.APPLICATION_XML_VALUE)
// //     @ResponseBody
// //     public String sitemapTirages() {

// //         List<Historique20Detail> tirages = detailService.getAllTirages();

// //         ZoneId paris = ZoneId.of("Europe/Paris");
// //         LocalDate today = LocalDate.now(paris);

// //         // ✅ Trier du plus récent au plus ancien (meilleur crawl)
// //         tirages.sort(Comparator.comparing(Historique20Detail::getDateDeTirage,
// //                 Comparator.nullsLast(Comparator.naturalOrder())).reversed());

// //         StringBuilder sb = new StringBuilder(16_384);
// //         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
// //         sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

// //         for (Historique20Detail t : tirages) {

// //             if (t.getDateDeTirage() == null) continue;

// //             LocalDate ld = t.getDateDeTirage()
// //                     .toInstant()
// //                     .atZone(paris)
// //                     .toLocalDate();

// //             DayOfWeek day = ld.getDayOfWeek();

// //             // ✅ Autoriser uniquement Lundi / Mercredi / Samedi
// //             if (day != DayOfWeek.MONDAY && day != DayOfWeek.WEDNESDAY && day != DayOfWeek.SATURDAY) {
// //                 continue;
// //             }

// //             String iso = ld.toString();
// //             long daysOld = ChronoUnit.DAYS.between(ld, today);

// //             // ✅ Priorité dynamique : plus récent = plus important
// //             String priority;
// //             if (daysOld <= 7) priority = "0.9";
// //             else if (daysOld <= 30) priority = "0.8";
// //             else if (daysOld <= 365) priority = "0.7";
// //             else priority = "0.6";

// //             sb.append("<url>");
// //             sb.append("<loc>").append(BASE_URL).append("/tirage/").append(iso).append("</loc>");
// //             sb.append("<lastmod>").append(iso).append("</lastmod>");
// //             sb.append("<changefreq>never</changefreq>");
// //             sb.append("<priority>").append(priority).append("</priority>");
// //             sb.append("</url>");
// //         }

// //         sb.append("</urlset>");
// //         return sb.toString();
// //     }
// // }

// package com.fdjloto.api.controller;

// import com.fdjloto.api.model.Historique20Detail;
// import com.fdjloto.api.service.Historique20DetailService;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.ResponseBody;

// import java.time.*;
// import java.time.format.DateTimeFormatter;
// import java.time.temporal.ChronoUnit;
// import java.util.Comparator;
// import java.util.List;

// @Controller
// public class SitemapController {

//     private static final String BASE_URL = "https://loto-tracker.fr";
//     private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

//     /**
//      * ✅ Limite crawl budget : garde seulement les derniers tirages
//      * 120 tirages ≈ ~40 semaines à 3 tirages/semaine
//      */
//     private static final int MAX_TIRAGES_IN_SITEMAP = 120;

//     private final Historique20DetailService detailService;

//     public SitemapController(Historique20DetailService detailService) {
//         this.detailService = detailService;
//     }

//     /**
//      * ✅ Sitemap index (à soumettre dans Google Search Console)
//      * lastmod = date du dernier tirage dispo (signal "nouveau contenu")
//      */
//     @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
//     @ResponseBody
//     public String sitemapIndex() {

//         String lastmod = getLastTirageDateIso(); // ✅ meilleur que "today"

//         return """
//             <?xml version="1.0" encoding="UTF-8"?>
//             <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
//               <sitemap>
//                 <loc>%s/sitemap-pages.xml</loc>
//                 <lastmod>%s</lastmod>
//               </sitemap>
//               <sitemap>
//                 <loc>%s/sitemap-tirages.xml</loc>
//                 <lastmod>%s</lastmod>
//               </sitemap>
//               <sitemap>
//                 <loc>%s/sitemap-static.xml</loc>
//                 <lastmod>%s</lastmod>
//               </sitemap>
//             </sitemapindex>
//             """.formatted(BASE_URL, lastmod, BASE_URL, lastmod, BASE_URL, LocalDate.now(PARIS));
//     }

//     /**
//      * ✅ Pages principales
//      */
//     @GetMapping(value = "/sitemap-pages.xml", produces = MediaType.APPLICATION_XML_VALUE)
//     @ResponseBody
//     public String sitemapPages() {

//         String lastmod = getLastTirageDateIso();

//         return """
//             <?xml version="1.0" encoding="UTF-8"?>
//             <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
//               <url>
//                 <loc>%s/</loc>
//                 <lastmod>%s</lastmod>
//                 <changefreq>daily</changefreq>
//                 <priority>1.0</priority>
//               </url>

//               <url>
//                 <loc>%s/dernier-tirage</loc>
//                 <lastmod>%s</lastmod>
//                 <changefreq>daily</changefreq>
//                 <priority>0.9</priority>
//               </url>

//               <!-- (optionnel mais utile SEO) une page stable pour "aujourd'hui" -->
//               <url>
//                 <loc>%s/resultat-loto-aujourdhui</loc>
//                 <lastmod>%s</lastmod>
//                 <changefreq>daily</changefreq>
//                 <priority>0.9</priority>
//               </url>
//             </urlset>
//             """.formatted(BASE_URL, lastmod, BASE_URL, lastmod, BASE_URL, lastmod);
//     }

//     /**
//      * ✅ Pages statiques (légales / info)
//      */
//     @GetMapping(value = "/sitemap-static.xml", produces = MediaType.APPLICATION_XML_VALUE)
//     @ResponseBody
//     public String sitemapStatic() {

//         String lastmod = LocalDate.now(PARIS).toString();

//         return """
//             <?xml version="1.0" encoding="UTF-8"?>
//             <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
//               <url>
//                 <loc>%s/politique_confidentialite.html</loc>
//                 <lastmod>%s</lastmod>
//                 <changefreq>yearly</changefreq>
//                 <priority>0.3</priority>
//               </url>
//             </urlset>
//             """.formatted(BASE_URL, lastmod);
//     }

//     /**
//      * ✅ Pages SEO par date : /tirage/yyyy-MM-dd
//      * Optimisé crawl : tri récent -> ancien + limite MAX + changefreq adapté
//      */
//     @GetMapping(value = "/sitemap-tirages.xml", produces = MediaType.APPLICATION_XML_VALUE)
//     @ResponseBody
//     public String sitemapTirages() {

//         List<Historique20Detail> tirages = detailService.getAllTirages();

//         // ✅ Trier du plus récent au plus ancien (meilleur crawl)
//         tirages.sort(Comparator.comparing(Historique20Detail::getDateDeTirage,
//                 Comparator.nullsLast(Comparator.naturalOrder())).reversed());

//         LocalDate today = LocalDate.now(PARIS);

//         StringBuilder sb = new StringBuilder(32_768);
//         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//         sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

//         int added = 0;

//         for (Historique20Detail t : tirages) {

//             if (t.getDateDeTirage() == null) continue;

//             LocalDate ld = t.getDateDeTirage().toInstant().atZone(PARIS).toLocalDate();
//             DayOfWeek day = ld.getDayOfWeek();

//             // ✅ Loto : uniquement Lundi / Mercredi / Samedi
//             if (day != DayOfWeek.MONDAY && day != DayOfWeek.WEDNESDAY && day != DayOfWeek.SATURDAY) {
//                 continue;
//             }

//             String iso = ld.toString();
//             long daysOld = ChronoUnit.DAYS.between(ld, today);

//             // ✅ Priorité dynamique : plus récent = plus important
//             String priority;
//             String changefreq;

//             if (daysOld <= 7) {
//                 priority = "0.9";
//                 changefreq = "daily";     // dernière semaine -> Google revient souvent
//             } else if (daysOld <= 30) {
//                 priority = "0.8";
//                 changefreq = "weekly";
//             } else if (daysOld <= 365) {
//                 priority = "0.7";
//                 changefreq = "monthly";
//             } else {
//                 priority = "0.9";
//                 changefreq = "yearly";
//             }

//             sb.append("<url>");
//             sb.append("<loc>").append(BASE_URL).append("/tirage/").append(iso).append("</loc>");
//             sb.append("<lastmod>").append(iso).append("</lastmod>");
//             sb.append("<changefreq>").append(changefreq).append("</changefreq>");
//             sb.append("<priority>").append(priority).append("</priority>");
//             sb.append("</url>");

//             added++;
//             if (added >= MAX_TIRAGES_IN_SITEMAP) break; // ✅ crawl budget
//         }

//         sb.append("</urlset>");
//         return sb.toString();
//     }

//     /**
//      * ✅ Récupère la date du dernier tirage (ISO yyyy-MM-dd)
//      * Si aucun tirage : fallback = aujourd'hui
//      */
//     private String getLastTirageDateIso() {

//         try {
//             List<Historique20Detail> tirages = detailService.getAllTirages();
//             if (tirages == null || tirages.isEmpty()) return LocalDate.now(PARIS).toString();

//             tirages.sort(Comparator.comparing(Historique20Detail::getDateDeTirage,
//                     Comparator.nullsLast(Comparator.naturalOrder())).reversed());

//             for (Historique20Detail t : tirages) {
//                 if (t.getDateDeTirage() == null) continue;
//                 LocalDate ld = t.getDateDeTirage().toInstant().atZone(PARIS).toLocalDate();
//                 DayOfWeek day = ld.getDayOfWeek();
//                 if (day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY || day == DayOfWeek.SATURDAY) {
//                     return ld.toString();
//                 }
//             }

//             return LocalDate.now(PARIS).toString();
//         } catch (Exception e) {
//             return LocalDate.now(PARIS).toString();
//         }
//     }
// }
