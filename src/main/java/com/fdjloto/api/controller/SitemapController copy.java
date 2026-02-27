// // package com.fdjloto.api.controller;

// // import org.springframework.http.MediaType;
// // import org.springframework.stereotype.Controller;
// // import org.springframework.web.bind.annotation.GetMapping;
// // import org.springframework.web.bind.annotation.ResponseBody;

// // @Controller
// // public class SitemapController {

// //     @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
// //     @ResponseBody
// //     public String sitemap() {

// //         String baseUrl = "https://loto-tracker.fr";

// //         return """
// //             <?xml version="1.0" encoding="UTF-8"?>
// //             <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
// //               <url>
// //                 <loc>%s/</loc>
// //                 <changefreq>daily</changefreq>
// //                 <priority>1.0</priority>
// //               </url>
// //               <url>
// //                 <loc>%s/dernier-tirage</loc>
// //                 <changefreq>daily</changefreq>
// //                 <priority>0.9</priority>
// //               </url>
// //             </urlset>
// //             """.formatted(baseUrl, baseUrl);
// //     }
// // }

// package com.fdjloto.api.controller;

// import com.fdjloto.api.model.Historique20Detail;
// import com.fdjloto.api.service.Historique20DetailService;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.ResponseBody;

// import java.time.DayOfWeek;
// import java.time.LocalDate;
// import java.time.ZoneId;
// import java.util.List;

// @Controller
// public class SitemapController {

//     private static final String BASE_URL = "https://loto-tracker.fr";
//     private final Historique20DetailService detailService;

//     public SitemapController(Historique20DetailService detailService) {
//         this.detailService = detailService;
//     }

//     /**
//      * ✅ Sitemap index (celui à déclarer dans Google Search Console)
//      */
//     @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
//     @ResponseBody
//     public String sitemapIndex() {
//         return """
//             <?xml version="1.0" encoding="UTF-8"?>
//             <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
//               <sitemap>
//                 <loc>%s/sitemap-static.xml</loc>
//               </sitemap>
//               <sitemap>
//                 <loc>%s/sitemap-tirages.xml</loc>
//               </sitemap>
//             </sitemapindex>
//             """.formatted(BASE_URL, BASE_URL);
//     }

//     /**
//      * ✅ Pages fixes
//      */
//     @GetMapping(value = "/sitemap-static.xml", produces = MediaType.APPLICATION_XML_VALUE)
//     @ResponseBody
//     public String sitemapStatic() {
//         return """
//             <?xml version="1.0" encoding="UTF-8"?>
//             <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
//               <url>
//                 <loc>%s/</loc>
//                 <changefreq>daily</changefreq>
//                 <priority>1.0</priority>
//               </url>
//               <url>
//                 <loc>%s/dernier-tirage</loc>
//                 <changefreq>daily</changefreq>
//                 <priority>0.9</priority>
//               </url>
//             </urlset>
//             """.formatted(BASE_URL, BASE_URL);
//     }

//     /**
//      * ✅ Pages SEO par date : /tirage/yyyy-MM-dd
//      */
// 	@GetMapping(value = "/sitemap-tirages.xml", produces = MediaType.APPLICATION_XML_VALUE)
// 	@ResponseBody
// 	public String sitemapTirages() {

// 		List<Historique20Detail> tirages = detailService.getAllTirages();

// 		StringBuilder sb = new StringBuilder(512);
// 		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
// 		sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

// 		ZoneId paris = ZoneId.of("Europe/Paris");
// 		LocalDate today = LocalDate.now(paris);

// 		for (Historique20Detail t : tirages) {

// 			if (t.getDateDeTirage() == null) continue;

// 			LocalDate ld = t.getDateDeTirage()
// 					.toInstant()
// 					.atZone(paris)
// 					.toLocalDate();

// 			DayOfWeek day = ld.getDayOfWeek();

// 			// 🔥 Autoriser uniquement Lundi / Mercredi / Samedi
// 			if (!(day == DayOfWeek.MONDAY
// 					|| day == DayOfWeek.WEDNESDAY
// 					|| day == DayOfWeek.SATURDAY)) {
// 				continue;
// 			}

// 			String iso = ld.toString();

// 			// 🔥 Priorité dynamique : plus récent = plus important
// 			long daysOld = java.time.temporal.ChronoUnit.DAYS.between(ld, today);

// 			String priority;
// 			if (daysOld <= 7) {
// 				priority = "0.9";
// 			} else if (daysOld <= 30) {
// 				priority = "0.8";
// 			} else {
// 				priority = "0.6";
// 			}

// 			sb.append("<url>");
// 			sb.append("<loc>").append(BASE_URL).append("/tirage/").append(iso).append("</loc>");
// 			sb.append("<lastmod>").append(iso).append("</lastmod>");
// 			sb.append("<changefreq>yearly</changefreq>");
// 			sb.append("<priority>").append(priority).append("</priority>");
// 			sb.append("</url>");
// 		}

// 		sb.append("</urlset>");
// 		return sb.toString();
// 	}
// }
