export function formaterHeure(iso: string): string {
  return new Date(iso).toLocaleTimeString("fr-FR", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function formaterDateHeure(iso: string): string {
  return new Date(iso).toLocaleString("fr-FR", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

/** Affichage seulement : la valeur vient de l'API, elle n'est jamais recalculée (F3). */
export function formaterNote(note: number): string {
  return note.toLocaleString("fr-FR", { maximumFractionDigits: 2 });
}
