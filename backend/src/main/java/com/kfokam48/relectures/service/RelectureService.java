package com.kfokam48.relectures.service;

import com.kfokam48.relectures.domain.Relecture;
import com.kfokam48.relectures.dto.RelectureAFaireDto;
import com.kfokam48.relectures.dto.RelectureRequete;
import com.kfokam48.relectures.exception.MetierException;
import com.kfokam48.relectures.repository.EtudiantRepository;
import com.kfokam48.relectures.repository.RelectureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class RelectureService {

    private final RelectureRepository relectureRepository;
    private final EtudiantRepository etudiantRepository;
    private final Clock horloge;

    public RelectureService(RelectureRepository relectureRepository, EtudiantRepository etudiantRepository,
                            Clock horloge) {
        this.relectureRepository = relectureRepository;
        this.etudiantRepository = etudiantRepository;
        this.horloge = horloge;
    }

    /**
     * EF6. appelantId vient de l'en-tete optionnel X-Etudiant-Id (H7).
     * Codes : 400 RELECTURE_INCONNUE, 403 AUTO_RELECTURE / RELECTEUR_NON_ATTRIBUE, 409 RELECTURE_DEJA_RENDUE.
     */
    @Transactional
    public void rendre(Long relectureId, RelectureRequete requete, Long appelantId) {
        // H12 : le contrat de cette operation imposee ne prevoit pas de 404
        Relecture relecture = relectureRepository.findById(relectureId)
                .orElseThrow(() -> new MetierException(HttpStatus.BAD_REQUEST, "RELECTURE_INCONNUE"));

        Long auteurId = relecture.getExercice().getAuteur().getId();
        Long relecteurId = relecture.getRelecteur().getId();

        // RG2 : controle de l'appelant, puis controle defensif de l'attribution
        if (appelantId != null && appelantId.equals(auteurId)) {
            throw new MetierException(HttpStatus.FORBIDDEN, "AUTO_RELECTURE");
        }
        if (appelantId != null && !appelantId.equals(relecteurId)) {
            throw new MetierException(HttpStatus.FORBIDDEN, "RELECTEUR_NON_ATTRIBUE");
        }
        if (relecteurId.equals(auteurId)) {
            throw new MetierException(HttpStatus.FORBIDDEN, "AUTO_RELECTURE");
        }

        // RG15 (Q15) : definitive une fois rendue
        if (relecture.estRendue()) {
            throw new MetierException(HttpStatus.CONFLICT, "RELECTURE_DEJA_RENDUE");
        }

        relecture.rendre(requete.note(), requete.commentaire().trim(), Instant.now(horloge));
    }

    /** Relectures attribuees a un etudiant, celles en attente d'abord. */
    @Transactional(readOnly = true)
    public List<RelectureAFaireDto> relecturesDe(Long etudiantId) {
        if (!etudiantRepository.existsById(etudiantId)) {
            throw new MetierException(HttpStatus.NOT_FOUND, "ETUDIANT_INCONNU");
        }
        return relectureRepository.findByRelecteurId(etudiantId).stream()
                .sorted(Comparator.comparing(Relecture::estRendue)
                        .thenComparing(Relecture::getAttribueeAt, Comparator.reverseOrder()))
                .map(RelectureAFaireDto::depuis)
                .toList();
    }
}