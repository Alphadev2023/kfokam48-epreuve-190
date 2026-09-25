import { useCallback, useEffect, useState } from "react";
import { deposerExercice, mesExercices } from "../api/exercices";
import { listerSessions } from "../api/sessions";
import type { ExerciceRecu, Session } from "../api/types";
import { libelleStatutExercice } from "../libelles";
import { Chargement } from "./Chargement";
import { MessageErreur } from "./MessageErreur";

interface Props {
  etudiantId: number;
  promotionId: number;
  rafraichissement: number;
}

interface Donnees {
  sessions: Session[];
  exercices: ExerciceRecu[];
}

/** H17 : le code de présence reçu avec les sessions n'est jamais affiché ici. */
export function DeposerExercice({
  etudiantId,
  promotionId,
  rafraichissement,
}: Props) {
  const [donnees, setDonnees] = useState<Donnees | null>(null);
  const [erreur, setErreur] = useState<unknown>(null);
  const [liens, setLiens] = useState<Record<number, string>>({});
  const [sessionEnEnvoi, setSessionEnEnvoi] = useState<number | null>(null);
  const [erreurDepot, setErreurDepot] = useState<{
    sessionId: number;
    erreur: unknown;
  } | null>(null);

  const charger = useCallback(() => {
    setErreur(null);
    Promise.all([listerSessions(promotionId), mesExercices(etudiantId)])
      .then(([sessions, exercices]) => setDonnees({ sessions, exercices }))
      .catch(setErreur);
  }, [promotionId, etudiantId]);

  useEffect(() => {
    charger();
  }, [charger, rafraichissement]);

  async function deposer(sessionId: number) {
    setSessionEnEnvoi(sessionId);
    setErreurDepot(null);
    try {
      await deposerExercice(
        sessionId,
        etudiantId,
        (liens[sessionId] ?? "").trim(),
      );
      setLiens((l) => ({ ...l, [sessionId]: "" }));
      charger();
    } catch (err) {
      setErreurDepot({ sessionId, erreur: err });
    } finally {
      setSessionEnEnvoi(null);
    }
  }

  if (erreur !== null) return <MessageErreur erreur={erreur} />;
  if (donnees === null)
    return <Chargement texte="Chargement de vos sessions..." />;

  const sessionsOuvertes = donnees.sessions.filter(
    (s) => s.statut === "OUVERTE",
  );

  return (
    <div>
      <h3>Mes exercices</h3>
      {sessionsOuvertes.length === 0 ? (
        <p>Aucune session ouverte pour votre promotion.</p>
      ) : (
        <ul className="liste-exercices">
          {sessionsOuvertes.map((s) => {
            const exercice = donnees.exercices.find(
              (e) => e.sessionId === s.id,
            );
            return (
              <li key={s.id}>
                <strong>{s.titre}</strong>
                {exercice ? (
                  <p>
                    <a href={exercice.lien} target="_blank" rel="noreferrer">
                      {exercice.lien}
                    </a>
                    <br />
                    {libelleStatutExercice(exercice.statut)}
                  </p>
                ) : (
                  <div className="form-depot">
                    <input
                      type="url"
                      placeholder="https://github.com/..."
                      value={liens[s.id] ?? ""}
                      onChange={(e) =>
                        setLiens((l) => ({ ...l, [s.id]: e.target.value }))
                      }
                    />
                    <button
                      type="button"
                      disabled={
                        sessionEnEnvoi === s.id ||
                        (liens[s.id] ?? "").trim() === ""
                      }
                      onClick={() => deposer(s.id)}
                    >
                      {sessionEnEnvoi === s.id ? "Envoi..." : "Déposer"}
                    </button>
                    {erreurDepot?.sessionId === s.id && (
                      <MessageErreur erreur={erreurDepot.erreur} />
                    )}
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
