import { useEffect, useState } from "react";
import { listerEtudiants } from "../api/promotions";
import type { EtudiantResume } from "../api/types";
import { useIdentite } from "../identite/IdentiteContext";
import { Chargement } from "./Chargement";
import { MessageErreur } from "./MessageErreur";
import { SelecteurPromotion } from "./SelecteurPromotion";

export function ChoixEtudiant() {
  const { choisir } = useIdentite();
  const [promotionId, setPromotionId] = useState<number | null>(null);
  const [etudiants, setEtudiants] = useState<EtudiantResume[] | null>(null);
  const [erreur, setErreur] = useState<unknown>(null);

  useEffect(() => {
    if (promotionId === null) return;
    setEtudiants(null);
    setErreur(null);
    listerEtudiants(promotionId).then(setEtudiants).catch(setErreur);
  }, [promotionId]);

  return (
    <div>
      <SelecteurPromotion valeur={promotionId} onChange={setPromotionId} />
      {promotionId !== null &&
        (erreur !== null ? (
          <MessageErreur erreur={erreur} />
        ) : etudiants === null ? (
          <Chargement texte="Chargement des étudiants..." />
        ) : (
          <ul className="liste-noms">
            {etudiants.map((e) => (
              <li key={e.id}>
                <button
                  type="button"
                  onClick={() =>
                    choisir({ etudiantId: e.id, nom: e.nom, promotionId })
                  }
                >
                  {e.nom}
                </button>
              </li>
            ))}
          </ul>
        ))}
    </div>
  );
}
