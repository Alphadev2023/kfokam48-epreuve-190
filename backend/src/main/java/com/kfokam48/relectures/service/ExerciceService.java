package com.kfokam48.relectures.service;

import com.kfokam48.relectures.domain.Etudiant;
import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.Relecture;
import com.kfokam48.relectures.domain.SessionCours;
import com.kfokam48.relectures.dto.ExerciceDeposeDto;
import com.kfokam48.relectures.dto.ExerciceRecuDto;
import com.kfokam48.relectures.dto.ExerciceRequete;
import com.kfokam48.relectures.exception.MetierException;
import com.kfokam48.relectures.repository.EtudiantRepository;
import com.kfokam48.relectures.repository.ExerciceRepository;
import com.kfokam48.relectures.repository.PresenceRepository;
import com.kfokam48.relectures.repository.RelectureRepository;
import com.kfokam48.relectures.repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExerciceService {

    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final ExerciceRepository exerciceRepository;
    private final RelectureRepository relectureRepository;
    private final AttributionService attributionService;
    private final Clock horloge;

    public ExerciceService(SessionRepository sessionRepository, EtudiantRepository etudiantRepository,
                           PresenceRepository presenceRepository, ExerciceRepository exerciceRepository,
                           RelectureRepository relectureRepository, AttributionService attributionService,
                           Clock horloge) {
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
        this.exerciceRepository = exerciceRepository;
        this.relectureRepository = relectureRepository;
        this.attributionService = attributionService;
        this.horloge = horloge;
    }

    @Transactional
    public ExerciceDeposeDto deposer(ExerciceRequete requete) {
        // H12 : le contrat de POST /api/exercices ne prevoit que 400 et 409
        SessionCours session = sessionRepository.findByIdAvecVerrou(requete.sessionId())
                .orElseThrow(() -> new MetierException(HttpStatus.BAD_REQUEST, "SESSION_INCONNUE"));
        Etudiant auteur = etudiantRepository.findById(requete.etudiantId())
                .orElseThrow(() -> new MetierException(HttpStatus.BAD_REQUEST, "ETUDIANT_INCONNU"));

        String lien = requete.lien().trim();
        if (!Exercice.lienValide(lien)) {
            throw new MetierException(HttpStatus.BAD_REQUEST, "LIEN_INVALIDE");                 // RG11
        }
        if (session.estCloturee()) {
            throw new MetierException(HttpStatus.CONFLICT, "SESSION_CLOTUREE");                 // RG10
        }
        if (!presenceRepository.existsBySessionIdAndEtudiantId(session.getId(), auteur.getId())) {
            throw new MetierException(HttpStatus.CONFLICT, "ETUDIANT_NON_PRESENT");             // RG9
        }
        if (exerciceRepository.existsBySessionIdAndAuteurId(session.getId(), auteur.getId())) {
            throw new MetierException(HttpStatus.CONFLICT, "EXERCICE_DEJA_DEPOSE");             // RG8
        }

        Exercice exercice = exerciceRepository.save(
                Exercice.deposer(session, auteur, lien, Instant.now(horloge)));

        attributionService.attribuer(exercice); // RG13, RG14
        return ExerciceDeposeDto.depuis(exercice);
    }

    @Transactional(readOnly = true)
    public List<ExerciceRecuDto> exercicesDe(Long etudiantId) {
        if (!etudiantRepository.existsById(etudiantId)) {
            throw new MetierException(HttpStatus.NOT_FOUND, "ETUDIANT_INCONNU");
        }
        List<Exercice> exercices = exerciceRepository.findByAuteurIdOrderByDeposeAtDesc(etudiantId);
        Map<Long, List<Relecture>> relecturesParExercice = relectureRepository
                .findByExerciceIdIn(exercices.stream().map(Exercice::getId).toList()).stream()
                .collect(Collectors.groupingBy(r -> r.getExercice().getId()));

        return exercices.stream()
                .map(e -> ExerciceRecuDto.depuis(e, relecturesParExercice.getOrDefault(e.getId(), List.of())))
                .toList();
    }

    /**
     * EF11 (Q13). Codes : 404 EXERCICE_INCONNU, 403 PAS_AUTEUR, 400 LIEN_INVALIDE,
     * 409 SESSION_CLOTUREE (RG10) ou RELECTURE_RENDUE (RG16).
     * Verrou sur l'exercice : meme ordre de verrouillage que le rendu d'une relecture (ENF8).
     */
    @Transactional
    public ExerciceDeposeDto remplacerLien(Long exerciceId, String nouveauLien, Long appelantId) {
        Exercice exercice = exerciceRepository.findByIdAvecVerrou(exerciceId)
                .orElseThrow(() -> new MetierException(HttpStatus.NOT_FOUND, "EXERCICE_INCONNU"));

        if (!exercice.getAuteur().getId().equals(appelantId)) {
            throw new MetierException(HttpStatus.FORBIDDEN, "PAS_AUTEUR");
        }
        String lien = nouveauLien.trim();
        if (!Exercice.lienValide(lien)) {
            throw new MetierException(HttpStatus.BAD_REQUEST, "LIEN_INVALIDE");                 // RG11
        }
        if (exercice.getSession().estCloturee()) {
            throw new MetierException(HttpStatus.CONFLICT, "SESSION_CLOTUREE");                 // RG10
        }
        if (relectureRepository.countByExerciceIdAndRendueAtIsNotNull(exercice.getId()) > 0) {
            throw new MetierException(HttpStatus.CONFLICT, "RELECTURE_RENDUE");                 // RG16
        }

        exercice.remplacerLien(lien, Instant.now(horloge));
        return ExerciceDeposeDto.depuis(exercice);
    }
}