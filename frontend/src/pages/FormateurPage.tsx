import { useCallback, useEffect, useState } from "react";
import type { FormEvent } from "react";
import { listerSessions, ouvrirSession } from "../api/sessions";
import type { Session, SessionOuverte } from "../api/types";
import { Chargement } from "../components/Chargement";
import { ExercicesEnAttente } from "../components/ExercicesEnAttente";
import { MessageErreur } from "../components/MessageErreur";
import { PresencesSession } from "../components/PresencesSession";
import { SelecteurPromotion } from "../components/SelecteurPromotion";
import { TableauPromotion } from "../components/TableauPromotion";
import { formaterDateHeure, formaterHeure } from "../format";

export default function FormateurPage() {
  const [promotionId, setPromotionId] = useState<number | null>(null);

  const [titre, setTitre] = useState("");
  const [envoiEnCours, setEnvoiEnCours] = useState(false);
  const [erreurOuverture, setErreurOuverture] = useState<unknown>(null);
  const [sessionOuverte, setSessionOuverte] = useState<SessionOuverte | null>(
    null,
  );

  const [sessions, setSessions] = useState<Session[] | null>(null);
  const [erreurSessions, setErreurSessions] = useState<unknown>(null);

  // EF9 (Q14) : séance dont on affiche les présences
  const [sessionSuivie, setSessionSuivie] = useState<Session | null>(null);

  const chargerSessions = useCallback((id: number) => {
    setSessions(null);
    setErreurSessions(null);
    listerSessions(id).then(setSessions).catch(setErreurSessions);
  }, []);

  useEffect(() => {
    if (promotionId !== null) chargerSessions(promotionId);
  }, [promotionId, chargerSessions]);

  async function ouvrir(e: FormEvent) {
    e.preventDefault();
    if (promotionId === null) return;
    setEnvoiEnCours(true);
    setErreurOuverture(null);
    try {
      const session = await ouvrirSession(titre, promotionId);
      setSessionOuverte(session);
      setTitre("");
      chargerSessions(promotionId);
    } catch (err) {
      setErreurOuverture(err);
    } finally {
      setEnvoiEnCours(false);
    }
  }

  return (
    <section>
      <h2>Espace formateur</h2>

      <SelecteurPromotion
        valeur={promotionId}
        onChange={(id) => {
          setPromotionId(id);
          setSessionOuverte(null);
          setSessionSuivie(null);
        }}
      />

      {promotionId !== null && (
        <>
          <h3>Ouvrir une session</h3>
          <form onSubmit={ouvrir}>
            <label>
              Titre de la séance{" "}
              <input
                value={titre}
                onChange={(e) => setTitre(e.target.value)}
                maxLength={200}
                placeholder="Séance 3 - Tests"
              />
            </label>{" "}
            <button
              type="submit"
              disabled={envoiEnCours || titre.trim() === ""}
            >
              {envoiEnCours ? "Ouverture..." : "Ouvrir la session"}
            </button>
          </form>
          {erreurOuverture !== null && (
            <MessageErreur erreur={erreurOuverture} />
          )}

          {sessionOuverte && (
            <div className="bloc-code">
              <p>Code de présence à communiquer aux étudiants :</p>
              <p className="code-presence">{sessionOuverte.code}</p>
              <p>
                Valable jusqu'à {formaterHeure(sessionOuverte.expirationAt)}
              </p>
            </div>
          )}

          <h3>Sessions de la promotion</h3>
          {erreurSessions !== null ? (
            <MessageErreur erreur={erreurSessions} />
          ) : sessions === null ? (
            <Chargement texte="Chargement des sessions..." />
          ) : sessions.length === 0 ? (
            <p>Aucune session pour cette promotion.</p>
          ) : (
            <div className="defilement">
              <table>
                <thead>
                  <tr>
                    <th>Titre</th>
                    <th>Ouverture</th>
                    <th>Code</th>
                    <th>Expiration du code</th>
                    <th>Statut</th>
                    <th>Présences</th>
                  </tr>
                </thead>
                <tbody>
                  {sessions.map((s) => (
                    <tr key={s.id}>
                      <td>{s.titre}</td>
                      <td>{formaterDateHeure(s.ouvertureAt)}</td>
                      <td>{s.code}</td>
                      <td>{formaterDateHeure(s.expirationAt)}</td>
                      <td>{s.statut === "OUVERTE" ? "Ouverte" : "Clôturée"}</td>
                      <td>
                        <button
                          type="button"
                          onClick={() => setSessionSuivie(s)}
                        >
                          Voir
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {sessionSuivie && (
            <PresencesSession
              session={sessionSuivie}
              promotionId={promotionId}
              onFermer={() => setSessionSuivie(null)}
            />
          )}

          <TableauPromotion promotionId={promotionId} />

          <ExercicesEnAttente promotionId={promotionId} />
        </>
      )}
    </section>
  );
}
