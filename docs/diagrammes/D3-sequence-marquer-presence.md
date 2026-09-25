# D3 — Séquence : marquer sa présence

Correspond à `POST /api/presences` dans `api/contrat.yaml`. Les contrôles sont faits dans cet ordre : code inconnu, code expiré ou session clôturée, étudiant, déjà présent.

```mermaid
sequenceDiagram
    autonumber
    actor E as Étudiant
    participant F as Front React (src/api/presences.ts)
    participant C as PresenceController
    participant S as PresenceService
    participant SR as SessionRepository
    participant PR as PresenceRepository
    participant A as AttributionService
    participant H as GlobalExceptionHandler

    E->>F: se choisit dans la liste (EF2) puis saisit le code
    F->>C: POST /api/presences {code, etudiantId}
    C->>S: marquer(code, etudiantId)
        S->>SR: findByCodeAvecVerrou(code) (verrou sur la session, ENF8)

    alt code inconnu (RG19)
        SR-->>S: aucune session
        S-->>H: lève CodeInconnuException (propagée par le contrôleur)
        H-->>F: 400 {code: "CODE_INCONNU", message}
        F-->>E: « Ce code n'existe pas. »
    else code expiré ou session clôturée (RG1, RG4)
        SR-->>S: session, maintenant après expirationAt
        S-->>H: lève CodeExpireException
        H-->>F: 410 {code: "CODE_EXPIRE", message}
        F-->>E: « Le code de présence a expiré. »
    else étudiant déjà présent (RG5)
        SR-->>S: session valide
        S->>PR: existsBySessionIdAndEtudiantId
        PR-->>S: vrai
        S-->>H: lève DejaPresentException
        H-->>F: 409 {code: "DEJA_PRESENT", message}
        F-->>E: « Ta présence est déjà enregistrée. »
    else cas nominal
        SR-->>S: session valide
        S->>PR: existsBySessionIdAndEtudiantId
        PR-->>S: faux
        S->>PR: save(Presence, source ETUDIANT)
        S->>A: attribuerExercicesEnAttente(session) (RG14)
        S-->>C: PresenceDto
        C-->>F: 201 {id, sessionId, etudiantId, source: "ETUDIANT"}
        F-->>E: « Présence enregistrée. »
    end
```

**Cas non détaillés dans le diagramme**, contrôlés entre l'expiration et le doublon : champ manquant (400 CHAMP_MANQUANT), étudiant inconnu (400 ETUDIANT_INCONNU) et étudiant d'une autre promotion (400 ETUDIANT_HORS_PROMOTION, RG6). L'enregistrement de la présence et l'attribution se font dans la même transaction.

**Depuis le correctif de l'issue #<B> :** la session est lue avec un verrou pessimiste (`SELECT ... FOR UPDATE`). Les marquages simultanés d'une même session sont traités l'un après l'autre. Avant ce correctif, deux marquages simultanés pouvaient tenter d'attribuer le même exercice en attente (RG14) ; l'un échouait et sa présence était perdue.
