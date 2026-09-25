package com.kfokam48.relectures.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ExerciceTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://github.com/etudiant/tp1",
            "http://exemple.cm/depot",
            "HTTPS://GITLAB.COM/a/b"
    })
    void rg11_liensAcceptes(String lien) {
        assertThat(Exercice.lienValide(lien)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "github.com/etudiant/tp1",
            "ftp://serveur/tp",
            "pas un lien",
            "https://",
            "javascript:alert(1)",
            " "
    })
    void rg11_liensRefuses(String lien) {
        assertThat(Exercice.lienValide(lien)).isFalse();
    }

    @Test
    void d4_rg21_lExerciceAttendSesDeuxRelecteursPuisSesDeuxRelectures() {
        Exercice exercice = Exercice.deposer(null, null, "https://x.cm/tp", Instant.now());
        assertThat(exercice.getRelecteursAttendus()).isEqualTo(2);

        exercice.relecteursAttribues(1);
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_ATTRIBUTION);

        exercice.relecteursAttribues(2);
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);

        exercice.relecturesRendues(1);
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.EN_ATTENTE_RELECTURE);

        exercice.relecturesRendues(2);
        assertThat(exercice.getStatut()).isEqualTo(StatutExercice.RELU);
    }
}