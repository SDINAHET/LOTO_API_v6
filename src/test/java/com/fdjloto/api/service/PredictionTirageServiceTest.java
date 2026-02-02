// package com.fdjloto.api.service;

// import com.fdjloto.api.model.PredictionTirageModel;
// import com.fdjloto.api.model.Tirage;
// import com.fdjloto.api.repository.PredictionRepository;
// import com.fdjloto.api.repository.TirageRepository;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Captor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.*;

// class PredictionTirageServiceTest {

//     private AutoCloseable mocks;

//     @Mock
//     TirageRepository tirageRepository;

//     @Mock
//     PredictionRepository predictionRepository;

//     @InjectMocks
//     PredictionTirageService service;

//     @Captor
//     ArgumentCaptor<PredictionTirageModel> predictionCaptor;

//     @BeforeEach
//     void setUp() {
//         mocks = MockitoAnnotations.openMocks(this);
//     }

//     @AfterEach
//     void tearDown() throws Exception {
//         if (mocks != null) mocks.close();
//     }

//     @Test
//     void generatePrediction_whenNoTirage_returnsNullAndDoesNotSave() {
//         when(tirageRepository.findAll()).thenReturn(List.of());

//         PredictionTirageModel result = service.generatePrediction();

//         assertThat(result).isNull();
//         verify(predictionRepository, never()).save(any());
//     }

//     @Test
//     void generatePrediction_whenTiragesExist_savesPrediction() {
//         Tirage t = new Tirage();
//         t.setBoules(new int[]{1, 2, 3, 4, 5});
//         t.setNumeroChance(9);
//         when(tirageRepository.findAll()).thenReturn(List.of(t));

//         PredictionTirageModel result = service.generatePrediction();

//         assertThat(result).isNotNull();
//         verify(predictionRepository).save(any(PredictionTirageModel.class));
//         assertThat(result.getProbableNumbers()).isNotNull();
//         assertThat(result.getProbableNumbers().size()).isBetween(1, 5);
//         assertThat(result.getProbableChance()).isEqualTo(9);
//     }

//     @Test
//     void generatePrediction_savesModelWithSortieRates() {
//         Tirage t1 = new Tirage();
//         t1.setBoules(new int[]{10, 11, 12, 13, 14});
//         t1.setNumeroChance(1);
//         Tirage t2 = new Tirage();
//         t2.setBoules(new int[]{10, 20, 30, 40, 49});
//         t2.setNumeroChance(1);

//         when(tirageRepository.findAll()).thenReturn(List.of(t1, t2));
//         when(predictionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

//         PredictionTirageModel result = service.generatePrediction();

//         assertThat(result).isNotNull();
//         assertThat(result.getSortieRates()).isNotNull();
//         // 10 apparaît 2 fois sur 10 boules => 20%
//         assertThat(result.getSortieRates().get(10)).isNotNull();
//     }

//     @Test
//     void generatePrediction_picksAtMost5Numbers() {
//         Tirage t = new Tirage();
//         t.setBoules(new int[]{1, 2, 3, 4, 5});
//         t.setNumeroChance(7);
//         // répéter pour gonfler les stats
//         when(tirageRepository.findAll()).thenReturn(List.of(t, t, t, t, t));

//         PredictionTirageModel result = service.generatePrediction();

//         assertThat(result).isNotNull();
//         assertThat(result.getProbableNumbers().size()).isLessThanOrEqualTo(5);
//     }
// }
