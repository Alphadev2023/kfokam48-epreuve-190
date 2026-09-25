package com.kfokam48.relectures.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * RG21 : la note retenue d'un exercice est la moyenne des notes de ses relectures rendues,
 * arrondie a 2 decimales. Elle est provisoire tant que moins de relectures que prevu sont rendues.
 */
public record NoteRetenue(double valeur, boolean provisoire) {

    public static Optional<NoteRetenue> calculer(List<Integer> notesRendues, int relecteursAttendus) {
        if (notesRendues.isEmpty()) {
            return Optional.empty();
        }
        double moyenne = notesRendues.stream().mapToInt(Integer::intValue).average().orElseThrow();
        return Optional.of(new NoteRetenue(arrondir(moyenne), notesRendues.size() < relecteursAttendus));
    }

    /** Meme regle a partir d'un agregat SQL (moyenne et nombre de relectures rendues). */
    public static Optional<NoteRetenue> depuisAgregat(Double moyenne, long rendues, int relecteursAttendus) {
        if (moyenne == null || rendues == 0) {
            return Optional.empty();
        }
        return Optional.of(new NoteRetenue(arrondir(moyenne), rendues < relecteursAttendus));
    }

    public static double arrondir(double valeur) {
        return BigDecimal.valueOf(valeur).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}