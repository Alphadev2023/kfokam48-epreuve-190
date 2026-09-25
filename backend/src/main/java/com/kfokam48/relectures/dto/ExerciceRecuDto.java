package com.kfokam48.relectures.dto;

import com.kfokam48.relectures.domain.Exercice;
import com.kfokam48.relectures.domain.NoteRetenue;
import com.kfokam48.relectures.domain.Relecture;
import com.kfokam48.relectures.domain.StatutExercice;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** EF12 : note retenue et commentaires, jamais le nom d'un relecteur (Q8, RG18). */
public record ExerciceRecuDto(Long id, Long sessionId, String sessionTitre, String lien,
                              StatutExercice statut, Double note, boolean noteProvisoire,
                              List<String> commentaires) {

    public static ExerciceRecuDto depuis(Exercice exercice, List<Relecture> relectures) {
        List<Relecture> rendues = relectures.stream()
                .filter(Relecture::estRendue)
                .sorted(Comparator.comparing(Relecture::getRendueAt))
                .toList();

        Optional<NoteRetenue> note = NoteRetenue.calculer(
                rendues.stream().map(Relecture::getNote).toList(),
                exercice.getRelecteursAttendus());

        return new ExerciceRecuDto(exercice.getId(), exercice.getSession().getId(),
                exercice.getSession().getTitre(), exercice.getLien(), exercice.getStatut(),
                note.map(NoteRetenue::valeur).orElse(null),
                note.map(NoteRetenue::provisoire).orElse(false),
                rendues.stream().map(Relecture::getCommentaire).toList());
    }
}