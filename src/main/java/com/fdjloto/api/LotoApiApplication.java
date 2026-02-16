// // filepath: /c:/Users/steph/Documents/portfolio/portfolio/Loto_API/src/main/java/com/fdjloto/api/LotoApiApplication.java
// package com.fdjloto.api;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.ComponentScan;
// // import org.springframework.context.annotation.Bean;
// import org.springframework.web.servlet.config.annotation.EnableWebMvc;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// // import io.swagger.v3.oas.models.OpenAPI;
// // import io.swagger.v3.oas.models.info.Info;

// // @SpringBootApplication
// @EnableWebMvc
// @ComponentScan(basePackages = "com.fdjloto.api")
// @SpringBootApplication(scanBasePackages = {"com.fdjloto"})
// public class LotoApiApplication {
//     public static void main(String[] args) {
//         SpringApplication.run(LotoApiApplication.class, args);
//     }

//     // @Bean
//     // public OpenAPI customOpenAPI() {
//     //     return new OpenAPI()
//     //             .info(new Info().title("Loto API").version("1.0").description("API pour la gestion des utilisateurs avec SQLite3"));
//     // }
// }


        // <p><b>Fonctionnalités principales :</b></p>
        // <ul>
        //     <li>🔑 <b>Authentification sécurisée :</b> Utilisation de JWT pour protéger les endpoints sensibles.</li>
        //     <li>👤 <b>Gestion des utilisateurs :</b>
        //         <ul>
        //             <li>Inscription, Connexion, Déconnexion sécurisée avec JWT.</li>
        //             <li>Gestion des rôles (Admin et Utilisateur) pour contrôler les accès aux fonctionnalités.</li>
        //         </ul>
        //     </li>
        //     <li>🎫 <b>Soumission et gestion des tickets :</b>
        //         <ul>
        //             <li>Création, mise à jour, suppression et consultation des tickets Loto.</li>
        //             <li>Stockage sécurisé des tickets en base de données.</li>
        //         </ul>
        //     </li>
        //     <li>💰 <b>Calcul automatique des gains :</b>
        //         <ul>
        //             <li>Comparaison des numéros soumis avec les résultats officiels FDJ.</li>
        //             <li>Calcul des gains pour chaque ticket soumis.</li>
        //         </ul>
        //     </li>
        //     <li>📅 <b>Historique des tickets :</b>
        //         <ul>
        //             <li>Consultation de l'historique des tickets joués.</li>
        //             <li>Accès aux détails des gains pour chaque ticket.</li>
        //         </ul>
        //     </li>
        //     <li>📊 <b>Visualisation des résultats :</b>
        //         <ul>
        //             <li>Affichage des résultats du Loto sous forme de graphiques dynamiques.</li>
        //             <li>Analyse des statistiques de tirage pour repérer des tendances.</li>
        //         </ul>
        //     </li>
        //     <li>📅 <b>Mise à jour automatique des résultats :</b>
        //         <ul>
        //             <li>Script journalier pour la récupération automatique des résultats FDJ.</li>
        //             <li>Synchronisation avec les bases de données pour un affichage en temps réel.</li>
        //         </ul>
        //     </li>
        //     <li>🔒 <b>Sécurité avancée :</b>
        //         <ul>
        //             <li>Protection des endpoints API avec Spring Security et JWT.</li>
        //             <li>Contrôle d'accès basé sur les rôles (Admin et Utilisateur).</li>
        //         </ul>
        //     </li>
        //     <li>🚀 <b>Déploiement et Hébergement :</b>
        //         <ul>
        //             <li>Déploiement sur Alwaysdata avec bases de données MySQL et MongoDB.</li>
        //             <li>Hébergement sécurisé avec accès public aux résultats du Loto.</li>
        //         </ul>
        //     </li>
        // </ul>
        // <p>🚧 <b>En développement :</b></p>
        // <ul>
        //     <li>🔔 Notifications push pour les résultats et les gains.</li>
        //     <li>🤖 Recommandations personnalisées basées sur l'historique des tickets.</li>
        // </ul>
                // <h3>🚧 En développement :</h3>
        // <ul style="padding-left: 20px; margin: 0;">
        //     <li>🔔 Notifications push pour les résultats et les gains.</li>
        //     <li>🤖 Recommandations personnalisées basées sur l'historique des tickets.</li>
        // </ul>


        // <p><i>Développé par Stéphane Dinahet pour le Portfolio 2025.</i></p>
        // <br>
        // <p><a href="http://localhost:8082") target="http://localhost:8082"> Serveur loca http://localhost:8082<p>
        // <p><a href="http://127.0.0.1:5500") target="http://127.0.0.1:5500"> Serveur local (Live Server http://127.0.0.1:5500)<p>
        // <br>
        // <p><b>Retrouvez-moi sur :</b></p>
        //     <p>
        //         <a href="https://github.com/SDINAHET" target="https://github.com/SDINAHET" style="text-decoration: none; color: inherit;">
        //             GitHub
        //         </a>
        //     </p>
        //     <p>
        //         <a href="https://www.linkedin.com/in/st%C3%A9phane-dinahet-3b363189/" target="https://www.linkedin.com/in/st%C3%A9phane-dinahet-3b363189/" style="text-decoration: none; color: inherit;">
        //             LinkedIn
        //         </a>
        //     </p>
// filepath: /c:/Users/steph/Documents/portfolio/portfolio/Loto_API/src/main/java/com/fdjloto/api/LotoApiApplication.java
package com.fdjloto.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
// import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;


// @EnableWebMvc
@ComponentScan(basePackages = "com.fdjloto.api")
@SpringBootApplication(scanBasePackages = {"com.fdjloto"})
@OpenAPIDefinition(
    info = @Info(
        title = "Loto Tracker API - Portfolio 2025-2026   Holberton School RENNES  🚀 ",
        version = "v2.0.0",
        description = """
        <h2>API permettant de suivre les résultats du Loto, de gérer les comptes utilisateurs, et de calculer automatiquement les gains en comparant les tickets soumis avec les résultats officiels du loto français.</h2>
        <h3>Fonctionnalités principales :</h3>
        <table style="border-collapse: collapse; width: 100%;">
            <thead>
                <tr style="background-color: #f2f2f2;">
                    <th style="border: 1px solid #ddd; padding: 2px; text-align: left;">Fonctionnalité</th>
                    <th style="border: 1px solid #ddd; padding: 2px; text-align: left;">Description</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td style="border: 1px solid #ddd; padding: 2px;">🔑 <b>Authentification sécurisée</b></td>
                    <td style="border: 1px solid #ddd; padding: 2px;">
                        <ul style="padding-left: 20px; margin: 0;">
                            <li>✔️ Utilisation de JWT pour protéger les endpoints sensibles.</li>
                        </ul>
                    </td>
                </tr>
                <tr>
                    <td style="border: 1px solid #ddd; padding: 2px;">👤 <b>Gestion des utilisateurs</b></td>
                    <td style="border: 1px solid #ddd; padding: 2px;">
                        <ul style="padding-left: 20px; margin: 0;">
                            <li>✔️ Inscription sans JWT.
                            <li>✔️ Connexion, Déconnexion sécurisée avec JWT.</li>
                            <li>✔️ Gestion des rôles (Admin et Utilisateur) pour contrôler les accès aux fonctionnalités.</li>
                        </ul>
                    </td>
                </tr>
                <tr>
                    <td style="border: 1px solid #ddd; padding: 2px;">🎫 <b>Soumission et gestion des tickets</b></td>
                    <td style="border: 1px solid #ddd; padding: 2px;">
                        <ul style="padding-left: 20px; margin: 0;">
                            <li>✔️ Création, mise à jour, suppression et consultation des tickets Loto (CRUD).</li>
                            <li>✔️ Stockage sécurisé des tickets en base de données.</li>
                        </ul>
                    </td>
                </tr>
                <tr>
                    <td style="border: 1px solid #ddd; padding: 2px;">💰 <b>Calcul automatique des gains (🚧 En développement)</b></td>
                    <td style="border: 1px solid #ddd; padding: 2px;">
                        <ul style="padding-left: 20px; margin: 0;">
                            <li>✔️ Comparaison des numéros soumis avec les résultats officiels FDJ.</li>
                            <li>✔️ Calcul des gains pour chaque ticket soumis.</li>
                        </ul>
                    </td>
                </tr>
                <tr>
                    <td style="border: 1px solid #ddd; padding: 2px;">📅 <b>Historique des tickets du joueur(= user)</b></td>
                    <td style="border: 1px solid #ddd; padding: 2px;">
                        <ul style="padding-left: 20px; margin: 0;">
                            <li>✔️ Consultation de l'historique des tickets joués.</li>
                            <li>✔️ Accès aux détails des gains pour chaque ticket.</li>
                        </ul>
                    </td>
                </tr>
                                <tr>
                    <td style="border: 1px solid #ddd; padding: 2px;">📅 <b>Historique des résultats du loto FDJ depuis 2019</b></td>
                    <td style="border: 1px solid #ddd; padding: 2px;">
                        <ul style="padding-left: 20px; margin: 0;">
                            <li>✔️ Consultation de l'historique des résultats officiels (public).</li>
                            <li>✔️ Accès aux détails des gains pour chaque tirage.</li>
                            <li>✔️ Clin d'oeil aux C, pour le tri des numéros d'un tirage dans l'ordre croissant.</li>
                            <li>✔️ Recherche d'un ancien tirage à une date précise ou une plage de dates.</li>
                        </ul>
                    </td>
                </tr>
                <tr>
                    <td style="border: 1px solid #ddd; padding: 2px;">📊 <b>Visualisation des résultats (🚧 En développement) </b></td>
                    <td style="border: 1px solid #ddd; padding: 2px;">
                        <ul style="padding-left: 20px; margin: 0;">
                            <li>✔️ Affichage des résultats du Loto sous forme de graphiques dynamiques.</li>
                            <li>✔️ Analyse des statistiques de tirage pour repérer des tendances.</li>
                        </ul>
                    </td>
                </tr>
                <tr>
                    <td style="border: 1px solid #ddd; padding: 2px;">📅 <b>Mise à jour automatique des résultats</b></td>
                    <td style="border: 1px solid #ddd; padding: 2px;">
                        <ul style="padding-left: 20px; margin: 0;">
                            <li>✔️ Script journalier pour la récupération automatique des résultats FDJ (toutes les 30 minutes).</li>
                            <li>✔️ Synchronisation avec les bases de données pour un affichage en temps réel.</li>
                        </ul>
                    </td>
                </tr>
                <tr>
                    <td style="border: 1px solid #ddd; padding: 2px;">🔒 <b>Sécurité avancée</b></td>
                    <td style="border: 1px solid #ddd; padding: 2px;">
                        <ul style="padding-left: 20px; margin: 0;">
                            <li>✔️ Protection des endpoints API avec Spring Security et JWT.</li>
                            <li>✔️ Contrôle d'accès basé sur les rôles (Admin et Utilisateur).</li>
                        </ul>
                    </td>
                </tr>
                <tr>
                    <td style="border: 1px solid #ddd; padding: 2px;">🚀 <b>Déploiement et Hébergement (🚧 En développement) </b></td>
                    <td style="border: 1px solid #ddd; padding: 2px;">
                        <ul style="padding-left: 20px; margin: 0;">
                            <li>Déploiement sur Alwaysdata avec bases de données MySQL ou postgreSQL et MongoDB.</li>
                            <li>Hébergement sécurisé avec accès public aux résultats du Loto.</li>
                        </ul>
                    </td>
                </tr>
                <tr>
                    <td style="border: 1px solid #ddd; padding: 2px;">🚧 <b>En développement (dans le futur) :</b></td>
                    <td style="border: 1px solid #ddd; padding: 2px;">
                        <ul style="padding-left: 20px; margin: 0;">
                            <li>🔔 Notifications push pour les résultats et les gains.</li>
                            <li>🤖 Recommandations personnalisées basées sur l'historique des tickets avec l'IA.</li>
                        </ul>
                </tr>
            </tbody>
        </table>
        """,
        contact = @Contact(
            name = "Stéphane DINAHET",
            email = "stephane.dinahet@gmail.com",
            url = "https://github.com/SDINAHET"
            )
        ),

    security = {
        @SecurityRequirement(name = "bearerAuth"),
        @SecurityRequirement(name = "jwtCookieAuth")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class LotoApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LotoApiApplication.class, args);
    }

   @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Loto Tracker API 🚀")
                        .version("v1.0.0")
                        .description("API pour suivre les résultats du Loto et gérer les comptes utilisateurs. <br> <b>Utilisation :</b> <ul><li>🔑 Authentification avec JWT</li><li>👤 Gestion des utilisateurs (Admin seulement)</li><li>📊 Accès public pour les résultats du Loto</li></ul>")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("Stéphane Dinahet")
                                .email("contact@fdjloto.com")
                                .url("https://github.com/SDINAHET")
                        )
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT"))
                )
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement()
                        .addList("bearerAuth"))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement()
                        .addList("jwtCookieAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                        .addSecuritySchemes("jwtCookieAuth",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY)
                                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.COOKIE)
                                        .name("jwtToken")
                                        .description("JWT Token dans le cookie 'jwtToken'")
                        ))
                .addServersItem(new Server().url("http://localhost:8082").description("Serveur local"))
                .addServersItem(new Server().url("http://127.0.0.1:5500").description("Serveur local (Live Server)"))
                // .addServersItem(new Server().url("http://localhost:8082/swagger-ui/index.html").description("Serveur local"))
                // .addServersItem(new Server().url("http://127.0.0.1:5500/src/main/resources/static/index.html").description("Serveur local (Live Server)"))
                .addServersItem(new Server().url("https://stephanedinahet.fr").description("Serveur Production1"))
                .addServersItem(new Server().url("https://loto-tracker.fr").description("Serveur Production2"));
                // .addServersItem(new Server().url("https://stephanedinahet.fr/swagger-ui/index.html").description("Serveur Production - documentation Swagger"));
    }

}
