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
