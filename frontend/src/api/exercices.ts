import { appelApi } from "./client";
import type { ExerciceDepose, ExerciceRecu } from "./types";

export function deposerExercice(
  sessionId: number,
  etudiantId: number,
  lien: string,
): Promise<ExerciceDepose> {
  return appelApi<ExerciceDepose>("/api/exercices", {
    method: "POST",
    body: JSON.stringify({ sessionId, etudiantId, lien }),
  });
}

export function mesExercices(etudiantId: number): Promise<ExerciceRecu[]> {
  return appelApi<ExerciceRecu[]>(`/api/etudiants/${etudiantId}/exercices`);
}
