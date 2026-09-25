package com.kfokam48.relectures.service;

import com.kfokam48.relectures.domain.Promotion;
import com.kfokam48.relectures.domain.SessionCours;
import com.kfokam48.relectures.dto.OuvrirSessionRequete;
import com.kfokam48.relectures.dto.SessionDto;
import com.kfokam48.relectures.dto.SessionOuverteDto;
import com.kfokam48.relectures.exception.MetierException;
import com.kfokam48.relectures.repository.PromotionRepository;
import com.kfokam48.relectures.repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SessionService {

    /** RG1 (Q2) : le code expire 15 minutes apres l'ouverture. */
    public static final Duration VALIDITE_CODE = Duration.ofMinutes(15);

    private static final int ESSAIS_CODE_MAX = 10;

    private final PromotionRepository promotionRepository;
    private final SessionRepository sessionRepository;
    private final GenerateurCode generateurCode;
    private final Clock horloge;

    public SessionService(PromotionRepository promotionRepository, SessionRepository sessionRepository,
                          GenerateurCode generateurCode, Clock horloge) {
        this.promotionRepository = promotionRepository;
        this.sessionRepository = sessionRepository;
        this.generateurCode = generateurCode;
        this.horloge = horloge;
    }

    @Transactional
    public SessionOuverteDto ouvrir(OuvrirSessionRequete requete) {
        // H12 : le contrat de POST /api/sessions ne prevoit que 400
        Promotion promotion = promotionRepository.findById(requete.promotionId())
                .orElseThrow(() -> new MetierException(HttpStatus.BAD_REQUEST, "PROMOTION_INCONNUE"));

        Instant ouverture = Instant.now(horloge).truncatedTo(ChronoUnit.SECONDS);
        SessionCours session = SessionCours.ouvrir(requete.titre().trim(), promotion,
                codeUnique(), ouverture, VALIDITE_CODE);

        return SessionOuverteDto.depuis(sessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public List<SessionDto> listerParPromotion(Long promotionId) {
        if (!promotionRepository.existsById(promotionId)) {
            throw new MetierException(HttpStatus.NOT_FOUND, "PROMOTION_INCONNUE");
        }
        return sessionRepository.findByPromotionIdOrderByOuvertureAtDesc(promotionId).stream()
                .map(SessionDto::depuis)
                .toList();
    }

    /** RG19 : le code identifie seul la session, il doit etre unique. */
    private String codeUnique() {
        for (int essai = 0; essai < ESSAIS_CODE_MAX; essai++) {
            String code = generateurCode.generer();
            if (!sessionRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de generer un code de presence unique");
    }
}