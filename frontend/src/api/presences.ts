import { appelApi } from "./client";
import type { Presence } from "./types";

export function marquerPresence(
  code: string,
  etudiantId: number,
): Promise<Presence> {
  return appelApi<Presence>("/api/presences", {
    method: "POST",
    body: JSON.stringify({ code, etudiantId }),
  });
}
