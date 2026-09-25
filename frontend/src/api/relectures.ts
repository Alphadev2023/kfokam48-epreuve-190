import { appelApi } from "./client";
import type { RelectureAFaire } from "./types";

export function mesRelectures(etudiantId: number): Promise<RelectureAFaire[]> {
  return appelApi<RelectureAFaire[]>(`/api/etudiants/${etudiantId}/relectures`);
}

/** H7 : l'identité choisie est transmise dans l'en-tête X-Etudiant-Id. */
export function rendreRelecture(
  relectureId: number,
  note: number,
  commentaire: string,
  etudiantId: number,
): Promise<void> {
  return appelApi<void>(`/api/relectures/${relectureId}`, {
    method: "POST",
    headers: { "X-Etudiant-Id": String(etudiantId) },
    body: JSON.stringify({ note, commentaire }),
  });
}
