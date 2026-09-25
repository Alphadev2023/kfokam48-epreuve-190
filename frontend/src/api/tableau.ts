import { appelApi } from "./client";
import type { LigneTableau } from "./types";

export function chargerTableau(promotionId: number): Promise<LigneTableau[]> {
  return appelApi<LigneTableau[]>(`/api/tableau?promotionId=${promotionId}`);
}
