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
import java.util.List;
import java.util.Map;
import java.util.Random;

/** EF5 : attribution automatique du relecteur (RG12, RG13, RG14, diagramme D4). */
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

    /** Au depot : tire un relecteur, ou laisse l'exercice EN_ATTENTE_ATTRIBUTION s'il n'y a aucun candidat. */
    @Transactional
    public void attribuer(Exercice exercice) {
        Long sessionId = exercice.getSession().getId();
        List<Long> presents = presenceRepository.findEtudiantIdsBySessionId(sessionId);

        TirageRelecteur.choisir(exercice.getAuteur().getId(), presents, chargesDansLaSession(sessionId), aleatoire)
                .ifPresent(relecteurId -> relectureRepository.save(Relecture.attribuer(
                        exercice, etudiantRepository.getReferenceById(relecteurId), Instant.now(horloge))));
    }

    /** RG14 : a chaque nouvelle presence, on retente l'attribution des exercices en attente de la session. */
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