package com.kfokam48.relectures.service;

import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.Relecture;
import com.kfokam48.relectures.domain.StatutExercice;
import com.kfokam48.relectures.repository.EtudiantRepository;
import com.kfokam48.relectures.repository.ExerciceRepository;
import com.kfokam48.relectures.repository.PresenceRepository;
import com.kfokam48.relectures.repository.RelectureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/** EF5 (v2) : attribution automatique de deux relecteurs distincts (RG12, RG13, RG14, D4). */
@Service
public class AttributionService {

    private final PresenceRepository presenceRepository;
    private final RelectureRepository relectureRepository;
    private final ExerciceRepository exerciceRepository;
    private final EtudiantRepository etudiantRepository;
    private final Clock horloge;
    private final Random aleatoire = new SecureRandom();

    public AttributionService(PresenceRepository presenceRepository, RelectureRepository relectureRepository,
                              ExerciceRepository exerciceRepository, EtudiantRepository etudiantRepository,
                              Clock horloge) {
        this.presenceRepository = presenceRepository;
        this.relectureRepository = relectureRepository;
        this.exerciceRepository = exerciceRepository;
        this.etudiantRepository = etudiantRepository;
        this.horloge = horloge;
    }

    /**
     * Tire les relecteurs manquants de l'exercice, autant que les presents le permettent.
     * S'il en manque encore, l'exercice reste EN_ATTENTE_ATTRIBUTION (RG14).
     */
    @Transactional
    public void attribuer(Exercice exercice) {
        List<Long> dejaAttribues = relectureRepository.findRelecteurIdsByExerciceId(exercice.getId());
        int manquants = exercice.getRelecteursAttendus() - dejaAttribues.size();
        if (manquants <= 0) {
            return;
        }

        Long sessionId = exercice.getSession().getId();
        List<Long> presents = presenceRepository.findEtudiantIdsBySessionId(sessionId);
        Map<Long, Long> charges = chargesDansLaSession(sessionId);
        Set<Long> exclus = new HashSet<>(dejaAttribues);
        Instant maintenant = Instant.now(horloge);

        for (int i = 0; i < manquants; i++) {
            Optional<Long> choix = TirageRelecteur.choisir(
                    exercice.getAuteur().getId(), exclus, presents, charges, aleatoire);
            if (choix.isEmpty()) {
                break;
            }
            Long relecteurId = choix.get();
            relectureRepository.save(Relecture.attribuer(
                    exercice, etudiantRepository.getReferenceById(relecteurId), maintenant));
            exclus.add(relecteurId);
            charges.merge(relecteurId, 1L, Long::sum);
        }

        exercice.relecteursAttribues(exclus.size());
    }

    /** RG14 : a chaque nouvelle presence, on complete les exercices encore en attente de relecteurs. */
    @Transactional
    public void attribuerExercicesEnAttente(Long sessionId) {
        for (Exercice exercice : exerciceRepository.findBySessionIdAndStatut(sessionId, StatutExercice.EN_ATTENTE_ATTRIBUTION)) {
            attribuer(exercice);
        }
    }

    private Map<Long, Long> chargesDansLaSession(Long sessionId) {
        Map<Long, Long> charges = new HashMap<>();
        for (Object[] ligne : relectureRepository.compterParRelecteurDansSession(sessionId)) {
            charges.put((Long) ligne[0], (Long) ligne[1]);
        }
        return charges;
    }
}