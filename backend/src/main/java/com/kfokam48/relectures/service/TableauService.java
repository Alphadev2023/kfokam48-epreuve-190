package com.kfokam48.relectures.service;

import com.kfokam48.relectures.domain.NoteRetenue;
import com.kfokam48.relectures.dto.LigneTableauDto;
import com.kfokam48.relectures.exception.MetierException;
import com.kfokam48.relectures.repository.EtudiantRepository;
import com.kfokam48.relectures.repository.ExerciceRepository;
import com.kfokam48.relectures.repository.PresenceRepository;
import com.kfokam48.relectures.repository.PromotionRepository;
import com.kfokam48.relectures.repository.RelectureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** EF7 (Q16) : 5 requetes agregees au total, quel que soit le nombre d'etudiants (ENF2). */
@Service
public class TableauService {

    private final PromotionRepository promotionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final ExerciceRepository exerciceRepository;
    private final RelectureRepository relectureRepository;

    public TableauService(PromotionRepository promotionRepository, EtudiantRepository etudiantRepository,
                          PresenceRepository presenceRepository, ExerciceRepository exerciceRepository,
                          RelectureRepository relectureRepository) {
        this.promotionRepository = promotionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
        this.exerciceRepository = exerciceRepository;
        this.relectureRepository = relectureRepository;
    }

    @Transactional(readOnly = true)
    public List<LigneTableauDto> tableau(Long promotionId) {
        if (!promotionRepository.existsById(promotionId)) {
            throw new MetierException(HttpStatus.NOT_FOUND, "PROMOTION_INCONNUE");
        }

        Map<Long, Long> presences = comptes(presenceRepository.compterParEtudiantDeLaPromotion(promotionId));
        Map<Long, Long> exercices = comptes(exerciceRepository.compterParAuteurDeLaPromotion(promotionId));
        Map<Long, Long> enAttente = comptes(relectureRepository.compterEnAttenteParRelecteurDeLaPromotion(promotionId));
        Map<Long, List<NoteRetenue>> notesParAuteur = notesRetenuesParAuteur(promotionId);

        return etudiantRepository.findByPromotionIdOrderByNomAsc(promotionId).stream()
                .map(e -> {
                    List<NoteRetenue> notes = notesParAuteur.getOrDefault(e.getId(), List.of());
                    Double moyenne = notes.isEmpty()
                            ? null
                            : NoteRetenue.arrondir(notes.stream().mapToDouble(NoteRetenue::valeur).average().orElseThrow());
                    boolean provisoire = notes.stream().anyMatch(NoteRetenue::provisoire);
                    return new LigneTableauDto(
                            e.getId(),
                            e.getNom(),
                            presences.getOrDefault(e.getId(), 0L).intValue(),
                            exercices.getOrDefault(e.getId(), 0L).intValue(),
                            moyenne,
                            provisoire,
                            enAttente.getOrDefault(e.getId(), 0L).intValue());
                })
                .toList();
    }

    /** RG17 : une note retenue par exercice ayant au moins une relecture rendue (RG21). */
    private Map<Long, List<NoteRetenue>> notesRetenuesParAuteur(Long promotionId) {
        Map<Long, List<NoteRetenue>> resultat = new HashMap<>();
        for (Object[] ligne : relectureRepository.notesRenduesParExerciceDeLaPromotion(promotionId)) {
            Long auteurId = (Long) ligne[0];
            int attendus = ((Number) ligne[2]).intValue();
            Double moyenneExercice = ((Number) ligne[3]).doubleValue();
            long rendues = ((Number) ligne[4]).longValue();
            NoteRetenue.depuisAgregat(moyenneExercice, rendues, attendus)
                    .ifPresent(note -> resultat.computeIfAbsent(auteurId, k -> new ArrayList<>()).add(note));
        }
        return resultat;
    }

    private static Map<Long, Long> comptes(List<Object[]> lignes) {
        Map<Long, Long> resultat = new HashMap<>();
        for (Object[] ligne : lignes) {
            resultat.put((Long) ligne[0], (Long) ligne[1]);
        }
        return resultat;
    }
}