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

    /** D4 : EN_ATTENTE_ATTRIBUTION vers EN_ATTENTE_RELECTURE. */
    public void relecteurAttribue() {
        if (statut != StatutExercice.EN_ATTENTE_ATTRIBUTION) {
            throw new IllegalStateException("Un relecteur est deja attribue a l'exercice " + id);
        }
        statut = StatutExercice.EN_ATTENTE_RELECTURE;
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

    public Instant getDeposeAt() {
        return deposeAt;
    }

    public Instant getModifieAt() {
        return modifieAt;
    }
}