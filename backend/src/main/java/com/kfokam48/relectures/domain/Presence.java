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

import java.time.Instant;

@Entity
@Table(name = "presence")
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionCours session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private SourcePresence source;

    @Column(name = "marquee_at", nullable = false)
    private Instant marqueeAt;

    protected Presence() {
    }

    private Presence(SessionCours session, Etudiant etudiant, SourcePresence source, Instant marqueeAt) {
        this.session = session;
        this.etudiant = etudiant;
        this.source = source;
        this.marqueeAt = marqueeAt;
    }

    public static Presence parEtudiant(SessionCours session, Etudiant etudiant, Instant maintenant) {
        return new Presence(session, etudiant, SourcePresence.ETUDIANT, maintenant);
    }

    /** RG7 (Q14) : la presence ajoutee a la main se voit. */
    public static Presence parFormateur(SessionCours session, Etudiant etudiant, Instant maintenant) {
        return new Presence(session, etudiant, SourcePresence.FORMATEUR, maintenant);
    }

    public Long getId() {
        return id;
    }

    public SessionCours getSession() {
        return session;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public SourcePresence getSource() {
        return source;
    }

    public Instant getMarqueeAt() {
        return marqueeAt;
    }
}