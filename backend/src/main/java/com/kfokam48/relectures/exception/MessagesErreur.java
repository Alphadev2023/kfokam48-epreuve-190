package com.kfokam48.relectures.exception;

import java.util.Map;

import static java.util.Map.entry;

/**
 * Libelles francais associes a chaque code d'erreur du contrat (api/contrat.yaml).
 */
public final class MessagesErreur {

    private static final Map<String, String> LIBELLES = Map.ofEntries(
            entry("REQUETE_INVALIDE", "La requête est mal formée."),
            entry("CHAMP_MANQUANT", "Un champ obligatoire est manquant."),
            entry("TITRE_TROP_LONG", "Le titre ne doit pas dépasser 200 caractères."),
            entry("RESSOURCE_INTROUVABLE", "Cette adresse n'existe pas."),
            entry("METHODE_NON_AUTORISEE", "Cette opération n'est pas autorisée sur cette adresse."),
            entry("ERREUR_INTERNE", "Une erreur interne est survenue. Réessayez plus tard."),
            entry("PROMOTION_INCONNUE", "Cette promotion n'existe pas."),
            entry("SESSION_INCONNUE", "Cette session n'existe pas."),
            entry("SESSION_CLOTUREE", "Cette session est clôturée."),
            entry("SESSION_DEJA_CLOTUREE", "Cette session est déjà clôturée."),
            entry("ETUDIANT_INCONNU", "Cet étudiant n'existe pas."),
            entry("ETUDIANT_HORS_PROMOTION", "Cet étudiant n'appartient pas à la promotion de cette session."),
            entry("ETUDIANT_NON_PRESENT", "Il faut être présent à la session pour déposer un exercice."),
            entry("CODE_INCONNU", "Ce code de présence n'existe pas."),
            entry("CODE_EXPIRE", "Le code de présence a expiré."),
            entry("DEJA_PRESENT", "La présence est déjà enregistrée pour cette session."),
            entry("LIEN_INVALIDE", "Le lien doit être une adresse http ou https complète."),
            entry("EXERCICE_DEJA_DEPOSE", "Un exercice a déjà été déposé pour cette session."),
            entry("EXERCICE_INCONNU", "Cet exercice n'existe pas."),
            entry("PAS_AUTEUR", "Seul l'auteur peut remplacer le lien de cet exercice."),
            entry("RELECTURE_RENDUE", "Le lien ne peut plus être remplacé : la relecture est rendue."),
            entry("RELECTURE_INCONNUE", "Cette relecture n'existe pas."),
            entry("NOTE_INVALIDE", "La note doit être un nombre entier entre 0 et 20."),
            entry("AUTO_RELECTURE", "Un étudiant ne peut pas relire son propre exercice."),
            entry("RELECTEUR_NON_ATTRIBUE", "Cette relecture est attribuée à un autre étudiant."),
            entry("RELECTURE_DEJA_RENDUE", "Cette relecture a déjà été rendue et ne peut plus être modifiée."),
            entry("COMMENTAIRE_TROP_LONG", "Le commentaire ne doit pas dépasser 2000 caractères.")
    );

    private MessagesErreur() {
    }

    public static String libelle(String code) {
        return LIBELLES.getOrDefault(code, "La requête est invalide.");
    }
}