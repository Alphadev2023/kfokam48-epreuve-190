export function Chargement({ texte = "Chargement..." }: { texte?: string }) {
  return <p role="status">{texte}</p>;
}
