import type { StatutExercice } from "./api/types";

const LIBELLES_STATUT_EXERCICE: Record<StatutExercice, string> = {
  EN_ATTENTE_ATTRIBUTION: "En attente d'un relecteur",
  EN_ATTENTE_RELECTURE: "En attente de relecture",
  RELU: "Relu",
};

export function libelleStatutExercice(statut: StatutExercice): string {
  return LIBELLES_STATUT_EXERCICE[statut];
}
