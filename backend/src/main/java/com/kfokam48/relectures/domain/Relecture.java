package com.kfokam48.relectures.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "relecture")
public class Relecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercice_id", nullable = false, unique = true)
    private Exercice exercice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relecteur_id", nullable = false)
    private Etudiant relecteur;

    private Integer note;

    @Column(length = 2000)
    private String commentaire;

    @Column(name = "attribuee_at", nullable = false)
    private Instant attribueeAt;

    @Column(name = "rendue_at")
    private Instant rendueAt;

    protected Relecture() {
    }

    /** RG2 : garde defensive, le tirage exclut deja l'auteur. RG12 : un seul relecteur par exercice. */
    public static Relecture attribuer(Exercice exercice, Etudiant relecteur, Instant maintenant) {
        if (relecteur.getId().equals(exercice.getAuteur().getId())) {
            throw new IllegalStateException("RG2 : un etudiant ne peut pas relire son propre exercice");
        }
        exercice.relecteurAttribue();
        Relecture relecture = new Relecture();
        relecture.exercice = exercice;
        relecture.relecteur = relecteur;
        relecture.attribueeAt = maintenant;
        return relecture;
    }

    public boolean estRendue() {
        return rendueAt != null;
    }

    /** RG15 (Q15) : une relecture rendue est definitive. */
    public void rendre(int note, String commentaire, Instant maintenant) {
        if (estRendue()) {
            throw new IllegalStateException("RG15 : relecture " + id + " deja rendue");
        }
        this.note = note;
        this.commentaire = commentaire;
        this.rendueAt = maintenant;
        exercice.marquerRelu();
    }

    public Long getId() {
        return id;
    }

    public Exercice getExercice() {
        return exercice;
    }

    public Etudiant getRelecteur() {
        return relecteur;
    }

    public Integer getNote() {
        return note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Instant getAttribueeAt() {
        return attribueeAt;
    }

    public Instant getRendueAt() {
        return rendueAt;
    }
}