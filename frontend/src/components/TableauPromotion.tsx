import { useCallback, useEffect, useState } from "react";
import { chargerTableau } from "../api/tableau";
import type { LigneTableau } from "../api/types";
import { Chargement } from "./Chargement";
import { MessageErreur } from "./MessageErreur";
import { formaterNote } from "../format";

/** F3 : la moyenne affichée est celle de l'API, jamais recalculée ici. */
export function TableauPromotion({ promotionId }: { promotionId: number }) {
  const [lignes, setLignes] = useState<LigneTableau[] | null>(null);
  const [erreur, setErreur] = useState<unknown>(null);

  const charger = useCallback(() => {
    setLignes(null);
    setErreur(null);
    chargerTableau(promotionId).then(setLignes).catch(setErreur);
  }, [promotionId]);

  useEffect(() => {
    charger();
  }, [charger]);

  return (
    <div>
      <h3>
        Tableau de la promotion{" "}
        <button type="button" onClick={charger}>
          Actualiser
        </button>
      </h3>
      {erreur !== null ? (
        <MessageErreur erreur={erreur} />
      ) : lignes === null ? (
        <Chargement texte="Chargement du tableau..." />
      ) : (
        <div className="defilement">
          <table>
            <thead>
              <tr>
                <th>Étudiant</th>
                <th>Présences</th>
                <th>Exercices déposés</th>
                <th>Moyenne reçue</th>
                <th>Relectures à rendre</th>
              </tr>
            </thead>
            <tbody>
              {lignes.map((l) => (
                <tr key={l.etudiantId}>
                  <td>{l.nom}</td>
                  <td>{l.presences}</td>
                  <td>{l.exercicesDeposes}</td>
                  <td>
                    {l.moyenne === null ? "—" : formaterNote(l.moyenne)}
                    {l.moyenne !== null && l.moyenneProvisoire && (
                      <span className="provisoire"> (provisoire)</span>
                    )}
                  </td>
                  <td
                    className={l.relecturesEnAttente > 0 ? "alerte" : undefined}
                  >
                    {l.relecturesEnAttente}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
