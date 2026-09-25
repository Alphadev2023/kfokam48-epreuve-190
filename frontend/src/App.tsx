import { useEffect, useState } from "react";
import { listerPromotions } from "./api/promotions";
import type { Promotion } from "./api/types";
import { Chargement } from "./components/Chargement";
import { MessageErreur } from "./components/MessageErreur";

export default function App() {
  const [promotions, setPromotions] = useState<Promotion[] | null>(null);
  const [erreur, setErreur] = useState<unknown>(null);

  useEffect(() => {
    listerPromotions().then(setPromotions).catch(setErreur);
  }, []);

  return (
    <main>
      <h1>KF48 Présences & Relectures</h1>
      {erreur ? (
        <MessageErreur erreur={erreur} />
      ) : promotions === null ? (
        <Chargement />
      ) : (
        <ul>
          {promotions.map((p) => (
            <li key={p.id}>{p.nom}</li>
          ))}
        </ul>
      )}
    </main>
  );
}
