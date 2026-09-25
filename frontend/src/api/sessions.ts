import { appelApi } from "./client";
import type { Session, SessionOuverte } from "./types";

export function ouvrirSession(
  titre: string,
  promotionId: number,
): Promise<SessionOuverte> {
  return appelApi<SessionOuverte>("/api/sessions", {
    method: "POST",
    body: JSON.stringify({ titre, promotionId }),
  });
}

export function listerSessions(promotionId: number): Promise<Session[]> {
  return appelApi<Session[]>(`/api/sessions?promotionId=${promotionId}`);
}
