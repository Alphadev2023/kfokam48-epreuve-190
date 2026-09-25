import { appelApi } from "./client";
import type { ExerciceEnAttente, LigneTableau } from "./types";

export function chargerTableau(promotionId: number): Promise<LigneTableau[]> {
  return appelApi<LigneTableau[]>(`/api/tableau?promotionId=${promotionId}`);
}

export function chargerExercicesEnAttente(
  promotionId: number,
): Promise<ExerciceEnAttente[]> {
  return appelApi<ExerciceEnAttente[]>(
    `/api/tableau/exercices-en-attente?promotionId=${promotionId}`,
  );
}
