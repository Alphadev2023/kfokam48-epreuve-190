import { useCallback, useEffect, useState } from "react";
import { mesRelectures } from "../api/relectures";
import type { RelectureAFaire } from "../api/types";
import { Chargement } from "../components/Chargement";
import { ChoixEtudiant } from "../components/ChoixEtudiant";
import { FormulaireRelecture } from "../components/FormulaireRelecture";
import { MessageErreur } from "../components/MessageErreur";
import { useIdentite } from "../identite/IdentiteContext";

export default function RelecteurPage() {
  const { identite, oublier } = useIdentite();
  const [relectures, setRelectures] = useState<RelectureAFaire[] | null>(null);
  const [erreur, setErreur] = useState<unknown>(null);

  const charger = useCallback(() => {
    if (!identite) return;
    setErreur(null);
    mesRelectures(identite.etudiantId).then(setRelectures).catch(setErreur);
  }, [identite]);

  useEffect(() => {
    charger();
  }, [charger]);

  if (!identite) {
    return (
      <section>
        <h2>Espace relecteur</h2>
        <p>Choisissez votre promotion, puis votre nom.</p>
        <ChoixEtudiant />
      </section>
    );
  }

  return (
    <section>
      <h2>Espace relecteur</h2>
      <p>
        Vous êtes <strong>{identite.nom}</strong>.{" "}
        <button type="button" onClick={oublier}>
          Ce n'est pas moi
        </button>
      </p>

      {erreur !== null ? (
        <MessageErreur erreur={erreur} />
      ) : relectures === null ? (
        <Chargement texte="Chargement de vos relectures..." />
      ) : relectures.length === 0 ? (
        <p>Aucune relecture ne vous est attribuée pour le moment.</p>
      ) : (
        <ul className="liste-exercices">
          {relectures.map((r) => (
            <li key={r.id}>
              <strong>{r.sessionTitre}</strong>
              <p>
                <a href={r.lien} target="_blank" rel="noreferrer">
                  {r.lien}
                </a>
              </p>
              {r.rendue ? (
                <p>
                  Relecture rendue : {r.note}/20. {r.commentaire}
                </p>
              ) : (
                <FormulaireRelecture
                  relecture={r}
                  etudiantId={identite.etudiantId}
                  onRendue={charger}
                />
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
