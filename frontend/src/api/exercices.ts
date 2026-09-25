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

/** EF11 : l'identité choisie est transmise dans l'en-tête X-Etudiant-Id (Q1). */
export function remplacerLien(
  exerciceId: number,
  lien: string,
  etudiantId: number,
): Promise<ExerciceDepose> {
  return appelApi<ExerciceDepose>(`/api/exercices/${exerciceId}`, {
    method: "PUT",
    headers: { "X-Etudiant-Id": String(etudiantId) },
    body: JSON.stringify({ lien }),
  });
}
