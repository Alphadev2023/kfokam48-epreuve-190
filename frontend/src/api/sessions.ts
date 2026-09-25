import { appelApi } from "./client";
import type {
  Presence,
  PresenceDetail,
  Session,
  SessionOuverte,
} from "./types";

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

export function ajouterPresence(
  sessionId: number,
  etudiantId: number,
): Promise<Presence> {
  return appelApi<Presence>(`/api/sessions/${sessionId}/presences`, {
    method: "POST",
    body: JSON.stringify({ etudiantId }),
  });
}

export function listerPresences(sessionId: number): Promise<PresenceDetail[]> {
  return appelApi<PresenceDetail[]>(`/api/sessions/${sessionId}/presences`);
}
