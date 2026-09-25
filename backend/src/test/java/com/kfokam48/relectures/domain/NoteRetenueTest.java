package com.kfokam48.relectures.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** RG21 : exemples tires des criteres d'acceptation de l'issue #25 (EF14). */
class NoteRetenueTest {

    @Test
    void deuxRelecturesRendues_moyenneDefinitive() {
        assertThat(NoteRetenue.calculer(List.of(13, 16), 2))
                .contains(new NoteRetenue(14.5, false));
    }

    @Test
    void uneSeuleRelectureSurDeux_noteProvisoire() {
        assertThat(NoteRetenue.calculer(List.of(12), 2))
                .contains(new NoteRetenue(12.0, true));
    }

    @Test
    void aucuneRelectureRendue_pasDeNote() {
        assertThat(NoteRetenue.calculer(List.of(), 2)).isEmpty();
    }

    @Test
    void rg22_exerciceDeposeAvantLeChangement_uneSeuleRelectureSuffit() {
        assertThat(NoteRetenue.calculer(List.of(15), 1))
                .contains(new NoteRetenue(15.0, false));
    }

    @Test
    void arrondiADeuxDecimales() {
        assertThat(NoteRetenue.arrondir(14.666666)).isEqualTo(14.67);
        assertThat(NoteRetenue.arrondir(14.665)).isEqualTo(14.67);
    }

    @Test
    void depuisAgregat_memeRegle() {
        assertThat(NoteRetenue.depuisAgregat(14.5, 2, 2)).contains(new NoteRetenue(14.5, false));
        assertThat(NoteRetenue.depuisAgregat(12.0, 1, 2)).contains(new NoteRetenue(12.0, true));
        assertThat(NoteRetenue.depuisAgregat(null, 0, 2)).isEmpty();
    }
}