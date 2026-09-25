package com.kfokam48.relectures.service;

import com.kfokam48.relectures.domain.Etudiant;
import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.Presence;
import com.kfokam48.relectures.domain.Promotion;
import com.kfokam48.relectures.domain.SessionCours;
import com.kfokam48.relectures.dto.PresenceRequete;
import com.kfokam48.relectures.exception.MetierException;
import com.kfokam48.relectures.repository.EtudiantRepository;
import com.kfokam48.relectures.repository.ExerciceRepository;
import com.kfokam48.relectures.repository.PresenceRepository;
import com.kfokam48.relectures.repository.PromotionRepository;
import com.kfokam48.relectures.repository.RelectureRepository;
import com.kfokam48.relectures.repository.SessionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bug signale a l'etape 3 : des marquages simultanes perdent des presences.
 * Pas de @Transactional : chaque marquage doit s'executer dans sa propre transaction, comme en production.
 */
@SpringBootTest
@ActiveProfiles("test")
class PresenceConcurrenceTest {

    private static final int NB_ETUDIANTS = 8;

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PresenceRepository presenceRepository;

    @Autowired
    private ExerciceRepository exerciceRepository;

    @Autowired
    private RelectureRepository relectureRepository;

    @AfterEach
    void nettoyer() {
        relectureRepository.deleteAllInBatch();
        exerciceRepository.deleteAllInBatch();
        presenceRepository.deleteAllInBatch();
        sessionRepository.deleteAllInBatch();
        etudiantRepository.deleteAllInBatch();
        promotionRepository.deleteAllInBatch();
    }

    @Test
    void desEtudiantsQuiMarquentEnMemeTempsSontTousEnregistres_memeAvecUnExerciceEnAttente() throws Exception {
        for (int manche = 0; manche < 3; manche++) {
            Promotion promo = promotionRepository.save(new Promotion("Promo concurrence " + manche));
            Etudiant auteur = etudiantRepository.save(new Etudiant("Auteur " + manche, promo));
            String code = "SIMUL" + (manche + 2);
            SessionCours session = sessionRepository.save(SessionCours.ouvrir("Seance " + manche, promo, code,
                    Instant.now(), Duration.ofMinutes(15)));

            // RG14 : l'auteur est seul present, son exercice attend un relecteur
            presenceRepository.save(Presence.parEtudiant(session, auteur, Instant.now()));
            exerciceRepository.save(Exercice.deposer(session, auteur, "https://github.com/auteur/tp", Instant.now()));

            List<Callable<String>> marquages = new ArrayList<>();
            for (int i = 0; i < NB_ETUDIANTS; i++) {
                Etudiant etudiant = etudiantRepository.save(new Etudiant("Etudiant " + manche + "-" + i, promo));
                marquages.add(marquer(code, etudiant.getId()));
            }

            List<String> resultats = enMemeTemps(marquages);

            assertThat(resultats)
                    .as("manche %d : chaque marquage simultane doit reussir", manche)
                    .containsOnly("OK");
            assertThat(presenceRepository.findEtudiantIdsBySessionId(session.getId()))
                    .as("manche %d : aucune presence perdue", manche)
                    .hasSize(NB_ETUDIANTS + 1);
        }
    }

    @Test
    void unDoubleEnvoiDuMemeEtudiantDonneUnePresenceEtDesRefus409() throws Exception {
        Promotion promo = promotionRepository.save(new Promotion("Promo double envoi"));
        Etudiant etudiant = etudiantRepository.save(new Etudiant("Double Envoi", promo));
        SessionCours session = sessionRepository.save(SessionCours.ouvrir("Seance double", promo, "DOUBL2",
                Instant.now(), Duration.ofMinutes(15)));

        List<Callable<String>> envois = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            envois.add(marquer("DOUBL2", etudiant.getId()));
        }

        List<String> resultats = enMemeTemps(envois);

        assertThat(resultats).filteredOn(r -> r.equals("OK")).hasSize(1);
        assertThat(resultats).filteredOn(r -> r.equals("DEJA_PRESENT")).hasSize(4);
        assertThat(presenceRepository.findEtudiantIdsBySessionId(session.getId())).hasSize(1);
    }

    private Callable<String> marquer(String code, Long etudiantId) {
        return () -> {
            try {
                presenceService.marquer(new PresenceRequete(code, etudiantId));
                return "OK";
            } catch (MetierException e) {
                return e.getCode();
            } catch (RuntimeException e) {
                return "ERREUR_TECHNIQUE:" + e.getClass().getSimpleName();
            }
        };
    }

    /** Lance toutes les taches au meme instant : chaque fil attend le signal de depart. */
    private List<String> enMemeTemps(List<Callable<String>> taches) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(taches.size());
        CountDownLatch prets = new CountDownLatch(taches.size());
        CountDownLatch depart = new CountDownLatch(1);
        try {
            List<Future<String>> futurs = new ArrayList<>();
            for (Callable<String> tache : taches) {
                futurs.add(pool.submit(() -> {
                    prets.countDown();
                    depart.await();
                    return tache.call();
                }));
            }
            prets.await(10, TimeUnit.SECONDS);
            depart.countDown();

            List<String> resultats = new ArrayList<>();
            for (Future<String> futur : futurs) {
                resultats.add(futur.get(30, TimeUnit.SECONDS));
            }
            return resultats;
        } finally {
            pool.shutdownNow();
        }
    }
}