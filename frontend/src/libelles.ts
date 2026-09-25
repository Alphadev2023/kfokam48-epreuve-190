import type { SourcePresence, StatutExercice } from "./api/types";

const LIBELLES_STATUT_EXERCICE: Record<StatutExercice, string> = {
  EN_ATTENTE_ATTRIBUTION: "En attente de relecteurs",
  EN_ATTENTE_RELECTURE: "En attente de relecture",
  RELU: "Relu",
};

export function libelleStatutExercice(statut: StatutExercice): string {
  return LIBELLES_STATUT_EXERCICE[statut];
}

/** Q14 : la présence ajoutée à la main doit se voir. */
export function libelleSource(source: SourcePresence): string {
  return source === "FORMATEUR" ? "ajouté par le formateur" : "par code";
}
