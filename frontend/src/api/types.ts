// Types alignés sur api/contrat.yaml

export interface Promotion {
  id: number;
  nom: string;
}

export type StatutSession = "OUVERTE" | "CLOTUREE";

export interface SessionOuverte {
  id: number;
  code: string;
  ouvertureAt: string;
  expirationAt: string;
}

export interface Session extends SessionOuverte {
  titre: string;
  promotionId: number;
  statut: StatutSession;
}

export interface EtudiantResume {
  id: number;
  nom: string;
}

export type SourcePresence = "ETUDIANT" | "FORMATEUR";

export interface Presence {
  id: number;
  sessionId: number;
  etudiantId: number;
  source: SourcePresence;
}

export type StatutExercice =
  | "EN_ATTENTE_ATTRIBUTION"
  | "EN_ATTENTE_RELECTURE"
  | "RELU";

export interface ExerciceDepose {
  id: number;
  statut: StatutExercice;
}

export interface ExerciceRecu {
  id: number;
  sessionId: number;
  sessionTitre: string;
  lien: string;
  statut: StatutExercice;
  note: number | null;
  commentaire: string | null;
}

export interface RelectureAFaire {
  id: number;
  exerciceId: number;
  sessionTitre: string;
  lien: string;
  rendue: boolean;
  note: number | null;
  commentaire: string | null;
}

export interface LigneTableau {
  etudiantId: number;
  nom: string;
  presences: number;
  exercicesDeposes: number;
  moyenne: number | null;
  relecturesEnAttente: number;
  moyenneProvisoire: boolean;
}

export interface ExerciceRecu {
  id: number;
  sessionId: number;
  sessionTitre: string;
  lien: string;
  statut: StatutExercice;
  note: number | null;
  noteProvisoire: boolean;
  commentaires: string[];
}

export interface RelecteurSuivi {
  nom: string;
  rendue: boolean;
}

export interface ExerciceEnAttente {
  exerciceId: number;
  sessionId: number;
  sessionTitre: string;
  auteurId: number;
  auteurNom: string;
  statut: StatutExercice;
  relecteursAttendus: number;
  relecteurs: RelecteurSuivi[];
}
