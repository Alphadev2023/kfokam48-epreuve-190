import { useEffect, useState } from "react";
import { listerPromotions } from "../api/promotions";
import type { Promotion } from "../api/types";
import { Chargement } from "./Chargement";
import { MessageErreur } from "./MessageErreur";

interface Props {
  valeur: number | null;
  onChange: (promotionId: number) => void;
}

export function SelecteurPromotion({ valeur, onChange }: Props) {
  const [promotions, setPromotions] = useState<Promotion[] | null>(null);
  const [erreur, setErreur] = useState<unknown>(null);

  useEffect(() => {
    listerPromotions().then(setPromotions).catch(setErreur);
  }, []);

  if (erreur) return <MessageErreur erreur={erreur} />;
  if (promotions === null)
    return <Chargement texte="Chargement des promotions..." />;

  return (
    <label>
      Promotion{" "}
      <select
        value={valeur ?? ""}
        onChange={(e) => onChange(Number(e.target.value))}
      >
        <option value="" disabled>
          Choisir une promotion
        </option>
        {promotions.map((p) => (
          <option key={p.id} value={p.id}>
            {p.nom}
          </option>
        ))}
      </select>
    </label>
  );
}
