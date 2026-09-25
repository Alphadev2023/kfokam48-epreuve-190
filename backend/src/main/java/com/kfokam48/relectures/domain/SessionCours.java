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

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "session_cours")
public class SessionCours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(nullable = false, length = 6, unique = true)
    private String code;

    @Column(name = "ouverture_at", nullable = false)
    private Instant ouvertureAt;

    @Column(name = "expiration_at", nullable = false)
    private Instant expirationAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private StatutSession statut;

    @Column(name = "cloture_at")
    private Instant clotureAt;

    protected SessionCours() {
    }

    /** RG1 : le code expire a ouverture + validite. */
    public static SessionCours ouvrir(String titre, Promotion promotion, String code,
                                      Instant ouverture, Duration validite) {
        SessionCours session = new SessionCours();
        session.titre = titre;
        session.promotion = promotion;
        session.code = code;
        session.ouvertureAt = ouverture;
        session.expirationAt = ouverture.plus(validite);
        session.statut = StatutSession.OUVERTE;
        return session;
    }

    /** RG1, RG4 : le code n'est utilisable que si la session est ouverte et que le code n'a pas expire. */
    public boolean codeValideA(Instant instant) {
        return statut == StatutSession.OUVERTE && instant.isBefore(expirationAt);
    }

    /** RG10 : apres la cloture, plus de depot ni de remplacement de lien. */
    public boolean estCloturee() {
        return statut == StatutSession.CLOTUREE;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public String getCode() {
        return code;
    }

    public Instant getOuvertureAt() {
        return ouvertureAt;
    }

    public Instant getExpirationAt() {
        return expirationAt;
    }

    public StatutSession getStatut() {
        return statut;
    }

    public Instant getClotureAt() {
        return clotureAt;
    }
}