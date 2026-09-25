import { appelApi } from "./client";
import type { EtudiantResume, Promotion } from "./types";

export function listerPromotions(): Promise<Promotion[]> {
  return appelApi<Promotion[]>("/api/promotions");
}

export function listerEtudiants(
  promotionId: number,
): Promise<EtudiantResume[]> {
  return appelApi<EtudiantResume[]>(`/api/promotions/${promotionId}/etudiants`);
}
