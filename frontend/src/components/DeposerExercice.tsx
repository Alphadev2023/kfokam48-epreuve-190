import { useCallback, useEffect, useState } from "react";
import { deposerExercice, mesExercices, remplacerLien } from "../api/exercices";
import { listerSessions } from "../api/sessions";
import type { ExerciceRecu, Session } from "../api/types";
import { formaterNote } from "../format";
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
  const [erreurEnvoi, setErreurEnvoi] = useState<{
    sessionId: number;
    erreur: unknown;
  } | null>(null);
  const [correction, setCorrection] = useState<{
    exerciceId: number;
    lien: string;
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
    setErreurEnvoi(null);
    try {
      await deposerExercice(
        sessionId,
        etudiantId,
        (liens[sessionId] ?? "").trim(),
      );
      setLiens((l) => ({ ...l, [sessionId]: "" }));
      charger();
    } catch (err) {
      setErreurEnvoi({ sessionId, erreur: err });
    } finally {
      setSessionEnEnvoi(null);
    }
  }

  async function enregistrerCorrection(sessionId: number) {
    if (!correction) return;
    setSessionEnEnvoi(sessionId);
    setErreurEnvoi(null);
    try {
      await remplacerLien(
        correction.exerciceId,
        correction.lien.trim(),
        etudiantId,
      );
      setCorrection(null);
      charger();
    } catch (err) {
      setErreurEnvoi({ sessionId, erreur: err });
    } finally {
      setSessionEnEnvoi(null);
    }
  }

  if (erreur !== null) return <MessageErreur erreur={erreur} />;
  if (donnees === null)
    return <Chargement texte="Chargement de vos sessions..." />;

  // EF10 : une séance clôturée reste affichée si l'étudiant y a déposé, pour qu'il voie sa note
  const sessionsAffichees = donnees.sessions.filter(
    (s) =>
      s.statut === "OUVERTE" ||
      donnees.exercices.some((e) => e.sessionId === s.id),
  );

  return (
    <div>
      <h3>Mes exercices</h3>
      {sessionsAffichees.length === 0 ? (
        <p>Aucune session ouverte pour votre promotion.</p>
      ) : (
        <ul className="liste-exercices">
          {sessionsAffichees.map((s) => {
            const exercice = donnees.exercices.find(
              (e) => e.sessionId === s.id,
            );
            // Aide d'affichage seulement : l'API refuse de toute façon (RG10, RG16)
            const corrigeable =
              exercice !== undefined &&
              s.statut === "OUVERTE" &&
              exercice.note === null;
            return (
              <li key={s.id}>
                <strong>{s.titre}</strong>
                {s.statut === "CLOTUREE" && (
                  <span className="provisoire"> (séance clôturée)</span>
                )}
                {exercice ? (
                  <div>
                    <p>
                      <a href={exercice.lien} target="_blank" rel="noreferrer">
                        {exercice.lien}
                      </a>
                      <br />
                      {libelleStatutExercice(exercice.statut)}
                    </p>
                    {exercice.note !== null && (
                      <p>
                        Note retenue :{" "}
                        <strong>{formaterNote(exercice.note)}/20</strong>
                        {exercice.noteProvisoire && (
                          <span className="provisoire">
                            {" "}
                            (provisoire, en attente d'une seconde relecture)
                          </span>
                        )}
                      </p>
                    )}
                    {exercice.commentaires.length > 0 && (
                      <ul className="commentaires">
                        {exercice.commentaires.map((commentaire, i) => (
                          <li key={i}>{commentaire}</li>
                        ))}
                      </ul>
                    )}
                    {corrigeable &&
                      (correction?.exerciceId === exercice.id ? (
                        <div className="form-depot">
                          <input
                            type="url"
                            value={correction.lien}
                            onChange={(e) =>
                              setCorrection({
                                exerciceId: exercice.id,
                                lien: e.target.value,
                              })
                            }
                          />
                          <button
                            type="button"
                            disabled={
                              sessionEnEnvoi === s.id ||
                              correction.lien.trim() === ""
                            }
                            onClick={() => enregistrerCorrection(s.id)}
                          >
                            {sessionEnEnvoi === s.id
                              ? "Envoi..."
                              : "Enregistrer le nouveau lien"}
                          </button>
                          <button
                            type="button"
                            onClick={() => setCorrection(null)}
                          >
                            Annuler
                          </button>
                        </div>
                      ) : (
                        <button
                          type="button"
                          onClick={() =>
                            setCorrection({
                              exerciceId: exercice.id,
                              lien: exercice.lien,
                            })
                          }
                        >
                          Corriger le lien
                        </button>
                      ))}
                    {erreurEnvoi?.sessionId === s.id && (
                      <MessageErreur erreur={erreurEnvoi.erreur} />
                    )}
                  </div>
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
                    {erreurEnvoi?.sessionId === s.id && (
                      <MessageErreur erreur={erreurEnvoi.erreur} />
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
