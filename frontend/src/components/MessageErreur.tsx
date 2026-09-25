import { ErreurApi } from "../api/client";

export function MessageErreur({ erreur }: { erreur: unknown }) {
  const texte =
    erreur instanceof ErreurApi
      ? erreur.message
      : "Une erreur inattendue est survenue.";
  return (
    <p role="alert" className="erreur">
      {texte}
    </p>
  );
}
