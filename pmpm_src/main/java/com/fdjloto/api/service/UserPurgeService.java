// package com.fdjloto.api.service;

// import com.fdjloto.api.model.User;
// import com.fdjloto.api.repository.UserRepository;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;

// import java.time.Instant;
// import java.time.temporal.ChronoUnit;
// import java.util.List;

// /**
//  * Purges users that were soft-deleted (deletedAt) more than 90 days ago.
//  * RGPD: data already anonymized at delete time; purge removes row.
//  */
// @Service
// public class UserPurgeService {

//     private static final Logger log = LoggerFactory.getLogger(UserPurgeService.class);
//     private final UserRepository userRepository;

//     public UserPurgeService(UserRepository userRepository) {
//         this.userRepository = userRepository;
//     }

//     @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Paris") // every day 03:00 Paris
//     public void purgeDeletedUsers() {
//         Instant threshold = Instant.now().minus(90, ChronoUnit.DAYS);
//         List<User> toPurge = userRepository.findAllByDeletedAtBefore(threshold);
//         if (toPurge.isEmpty()) return;

//         userRepository.deleteAll(toPurge);
//         log.info("Purged {} users deleted before {}", toPurge.size(), threshold);
//     }
// }
