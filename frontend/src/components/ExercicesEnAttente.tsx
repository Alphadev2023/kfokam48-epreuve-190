import { useCallback, useEffect, useState } from "react";
import { chargerExercicesEnAttente } from "../api/tableau";
import type { ExerciceEnAttente } from "../api/types";
import { libelleStatutExercice } from "../libelles";
import { Chargement } from "./Chargement";
import { MessageErreur } from "./MessageErreur";

/** EF8 (Q11) : le formateur voit clairement ce qui bloque. */
export function ExercicesEnAttente({ promotionId }: { promotionId: number }) {
  const [exercices, setExercices] = useState<ExerciceEnAttente[] | null>(null);
  const [erreur, setErreur] = useState<unknown>(null);

  const charger = useCallback(() => {
    setExercices(null);
    setErreur(null);
    chargerExercicesEnAttente(promotionId).then(setExercices).catch(setErreur);
  }, [promotionId]);

  useEffect(() => {
    charger();
  }, [charger]);

  return (
    <div>
      <h3>
        Exercices en attente{" "}
        <button type="button" onClick={charger}>
          Actualiser
        </button>
      </h3>
      {erreur !== null ? (
        <MessageErreur erreur={erreur} />
      ) : exercices === null ? (
        <Chargement texte="Chargement des exercices en attente..." />
      ) : exercices.length === 0 ? (
        <p>Aucun exercice en attente : toutes les relectures sont rendues.</p>
      ) : (
        <div className="defilement">
          <table>
            <thead>
              <tr>
                <th>Séance</th>
                <th>Auteur</th>
                <th>Statut</th>
                <th>Relecteurs</th>
              </tr>
            </thead>
            <tbody>
              {exercices.map((e) => {
                const manquants = e.relecteursAttendus - e.relecteurs.length;
                return (
                  <tr key={e.exerciceId}>
                    <td>{e.sessionTitre}</td>
                    <td>{e.auteurNom}</td>
                    <td>{libelleStatutExercice(e.statut)}</td>
                    <td>
                      {e.relecteurs.length === 0
                        ? "aucun"
                        : e.relecteurs.map((r, i) => (
                            <span
                              key={i}
                              className={r.rendue ? undefined : "alerte"}
                            >
                              {i > 0 && ", "}
                              {r.nom} ({r.rendue ? "rendue" : "en attente"})
                            </span>
                          ))}
                      {manquants > 0 && (
                        <span className="provisoire">
                          {" "}
                          — {manquants} à attribuer
                        </span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
