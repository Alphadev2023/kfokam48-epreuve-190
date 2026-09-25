package com.kfokam48.relectures.service;

import com.kfokam48.relectures.domain.Etudiant;
import com.kfokam48.relectures.domain.Presence;
import com.kfokam48.relectures.domain.SessionCours;
import com.kfokam48.relectures.dto.PresenceDto;
import com.kfokam48.relectures.dto.PresenceRequete;
import com.kfokam48.relectures.exception.MetierException;
import com.kfokam48.relectures.repository.EtudiantRepository;
import com.kfokam48.relectures.repository.PresenceRepository;
import com.kfokam48.relectures.repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

/** Ordre des controles : diagramme D3. */
@Service
public class PresenceService {

    private final SessionRepository sessionRepository;
    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final Clock horloge;

    public PresenceService(SessionRepository sessionRepository, EtudiantRepository etudiantRepository,
                           PresenceRepository presenceRepository, Clock horloge) {
        this.sessionRepository = sessionRepository;
        this.etudiantRepository = etudiantRepository;
        this.presenceRepository = presenceRepository;
        this.horloge = horloge;
    }

    @Transactional
    public PresenceDto marquer(PresenceRequete requete) {
        // Saisie sur telephone : on tolere minuscules et espaces
        String code = requete.code().trim().toUpperCase(Locale.ROOT);

        // RG19 : le code seul identifie la session
        SessionCours session = sessionRepository.findByCode(code)
                .orElseThrow(() -> new MetierException(HttpStatus.BAD_REQUEST, "CODE_INCONNU"));

        // RG1, RG4 : code expire ou session cloturee
        Instant maintenant = Instant.now(horloge);
        if (!session.codeValideA(maintenant)) {
            throw new MetierException(HttpStatus.GONE, "CODE_EXPIRE");
        }

        Etudiant etudiant = etudiantRepository.findById(requete.etudiantId())
                .orElseThrow(() -> new MetierException(HttpStatus.BAD_REQUEST, "ETUDIANT_INCONNU"));

        // RG6 (H9) : seulement dans une session de sa promotion
        if (!etudiant.getPromotion().getId().equals(session.getPromotion().getId())) {
            throw new MetierException(HttpStatus.BAD_REQUEST, "ETUDIANT_HORS_PROMOTION");
        }

        // RG5 : une seule presence par session
        if (presenceRepository.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
            throw new MetierException(HttpStatus.CONFLICT, "DEJA_PRESENT");
        }

        Presence presence = presenceRepository.save(Presence.parEtudiant(session, etudiant, maintenant));

        // RG14 : l'attribution des exercices en attente sera branchee ici par l'issue #6 (voir D3)
        return PresenceDto.depuis(presence);
    }
}