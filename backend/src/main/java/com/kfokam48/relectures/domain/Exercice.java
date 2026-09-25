package com.kfokam48.relectures.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;

@Entity
@Table(name = "exercice")
public class Exercice {

    public static final int LIEN_LONGUEUR_MAX = 500;

    /** RG12 (enveloppe, etape 3) : chaque exercice depose est relu par deux pairs. */
    public static final int RELECTEURS_PAR_EXERCICE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionCours session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant auteur;

    @Column(nullable = false, length = LIEN_LONGUEUR_MAX)
    private String lien;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private StatutExercice statut;

    /** RG22 : 1 pour les exercices deposes avant V101, 2 ensuite. */
    @Column(name = "relecteurs_attendus", nullable = false)
    private int relecteursAttendus;

    @Column(name = "depose_at", nullable = false)
    private Instant deposeAt;

    @Column(name = "modifie_at")
    private Instant modifieAt;

    protected Exercice() {
    }

    /** D4 : un exercice commence en attente d'attribution ; le tirage le fait avancer (RG13, RG14). */
    public static Exercice deposer(SessionCours session, Etudiant auteur, String lien, Instant maintenant) {
        Exercice exercice = new Exercice();
        exercice.session = session;
        exercice.auteur = auteur;
        exercice.lien = lien;
        exercice.statut = StatutExercice.EN_ATTENTE_ATTRIBUTION;
        exercice.relecteursAttendus = RELECTEURS_PAR_EXERCICE;
        exercice.deposeAt = maintenant;
        return exercice;
    }

    /** RG11 : URL absolue en http ou https, avec un hote. */
    public static boolean lienValide(String lien) {
        if (lien == null || lien.isBlank() || lien.length() > LIEN_LONGUEUR_MAX) {
            return false;
        }
        try {
            URI uri = new URI(lien.trim());
            String schema = uri.getScheme();
            return schema != null
                    && (schema.equalsIgnoreCase("http") || schema.equalsIgnoreCase("https"))
                    && uri.getHost() != null
                    && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /** D4 : l'exercice attend ses relectures des qu'il a tous ses relecteurs. */
    public void relecteursAttribues(int nombre) {
        if (statut == StatutExercice.EN_ATTENTE_ATTRIBUTION && nombre >= relecteursAttendus) {
            statut = StatutExercice.EN_ATTENTE_RELECTURE;
        }
    }

    /** D4, RG21 : l'exercice est relu quand toutes les relectures attendues sont rendues. */
    public void relecturesRendues(long nombre) {
        if (nombre >= relecteursAttendus) {
            statut = StatutExercice.RELU;
        }
    }

    /** EF11 (Q13) : les conditions (RG10, RG16) sont verifiees par le service. */
    public void remplacerLien(String nouveauLien, Instant maintenant) {
        this.lien = nouveauLien;
        this.modifieAt = maintenant;
    }

    public Long getId() {
        return id;
    }

    public SessionCours getSession() {
        return session;
    }

    public Etudiant getAuteur() {
        return auteur;
    }

    public String getLien() {
        return lien;
    }

    public StatutExercice getStatut() {
        return statut;
    }

    public int getRelecteursAttendus() {
        return relecteursAttendus;
    }

    public Instant getDeposeAt() {
        return deposeAt;
    }

    public Instant getModifieAt() {
        return modifieAt;
    }
}