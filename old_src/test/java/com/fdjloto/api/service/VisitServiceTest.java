// package com.fdjloto.api.service;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.io.TempDir;

// import java.nio.file.Path;

// import static org.assertj.core.api.Assertions.assertThat;

// class VisitServiceTest {

//     @Test
//     void incrementAndGet_incrementsAndPersists(@TempDir Path tmp) {
//         String originalUserDir = System.getProperty("user.dir");
//         try {
//             System.setProperty("user.dir", tmp.toString());

//             VisitService svc1 = new VisitService();
//             assertThat(svc1.get()).isEqualTo(0);
//             long v1 = svc1.incrementAndGet();
//             long v2 = svc1.incrementAndGet();
//             assertThat(v1).isEqualTo(1);
//             assertThat(v2).isEqualTo(2);

//             // nouvelle instance => relit le fichier data/visits.txt
//             VisitService svc2 = new VisitService();
//             assertThat(svc2.get()).isEqualTo(2);
//         } finally {
//             if (originalUserDir != null) System.setProperty("user.dir", originalUserDir);
//         }
//     }

//     @Test
//     void get_returnsCurrentValue(@TempDir Path tmp) {
//         String originalUserDir = System.getProperty("user.dir");
//         try {
//             System.setProperty("user.dir", tmp.toString());
//             VisitService svc = new VisitService();
//             assertThat(svc.get()).isEqualTo(0);
//             svc.incrementAndGet();
//             assertThat(svc.get()).isEqualTo(1);
//         } finally {
//             if (originalUserDir != null) System.setProperty("user.dir", originalUserDir);
//         }
//     }
// }
