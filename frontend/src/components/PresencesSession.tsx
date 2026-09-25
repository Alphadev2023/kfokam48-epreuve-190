import { useCallback, useEffect, useState } from "react";
import { listerEtudiants } from "../api/promotions";
import { ajouterPresence, listerPresences } from "../api/sessions";
import type { EtudiantResume, PresenceDetail, Session } from "../api/types";
import { formaterHeure } from "../format";
import { libelleSource } from "../libelles";
import { Chargement } from "./Chargement";
import { MessageErreur } from "./MessageErreur";

interface Props {
  session: Session;
  promotionId: number;
  onFermer: () => void;
}

interface Donnees {
  presences: PresenceDetail[];
  etudiants: EtudiantResume[];
}

/** EF9 (Q14) : présences d'une séance, avec ajout manuel visible. */
export function PresencesSession({ session, promotionId, onFermer }: Props) {
  const [donnees, setDonnees] = useState<Donnees | null>(null);
  const [erreur, setErreur] = useState<unknown>(null);
  const [etudiantChoisi, setEtudiantChoisi] = useState<number | null>(null);
  const [envoiEnCours, setEnvoiEnCours] = useState(false);
  const [erreurAjout, setErreurAjout] = useState<unknown>(null);

  const charger = useCallback(() => {
    setErreur(null);
    Promise.all([listerPresences(session.id), listerEtudiants(promotionId)])
      .then(([presences, etudiants]) => setDonnees({ presences, etudiants }))
      .catch(setErreur);
  }, [session.id, promotionId]);

  useEffect(() => {
    charger();
  }, [charger]);

  async function ajouter() {
    if (etudiantChoisi === null) return;
    setEnvoiEnCours(true);
    setErreurAjout(null);
    try {
      await ajouterPresence(session.id, etudiantChoisi);
      setEtudiantChoisi(null);
      charger();
    } catch (err) {
      setErreurAjout(err);
    } finally {
      setEnvoiEnCours(false);
    }
  }

  const absents = donnees
    ? donnees.etudiants.filter(
        (e) => !donnees.presences.some((p) => p.etudiantId === e.id),
      )
    : [];

  return (
    <div className="bloc-code">
      <h3>
        Présences — {session.titre}{" "}
        <button type="button" onClick={onFermer}>
          Fermer
        </button>
      </h3>

      {erreur !== null ? (
        <MessageErreur erreur={erreur} />
      ) : donnees === null ? (
        <Chargement texte="Chargement des présences..." />
      ) : (
        <>
          <p>
            {donnees.presences.length} présent(s) sur {donnees.etudiants.length}
          </p>
          <ul className="liste-presences">
            {donnees.presences.map((p) => (
              <li key={p.etudiantId}>
                {p.nom} — {formaterHeure(p.marqueeAt)}
                {p.source === "FORMATEUR" && (
                  <strong className="provisoire">
                    {" "}
                    ({libelleSource(p.source)})
                  </strong>
                )}
              </li>
            ))}
          </ul>

          {session.statut === "CLOTUREE" ? (
            <p>Séance clôturée : plus aucune présence ne peut être ajoutée.</p>
          ) : (
            <>
              <h4>Ajouter une présence à la main</h4>
              <div className="form-depot">
                <select
                  value={etudiantChoisi ?? ""}
                  onChange={(e) => setEtudiantChoisi(Number(e.target.value))}
                >
                  <option value="" disabled>
                    Choisir un étudiant absent
                  </option>
                  {absents.map((e) => (
                    <option key={e.id} value={e.id}>
                      {e.nom}
                    </option>
                  ))}
                </select>
                <button
                  type="button"
                  disabled={envoiEnCours || etudiantChoisi === null}
                  onClick={ajouter}
                >
                  {envoiEnCours ? "Ajout..." : "Ajouter la présence"}
                </button>
                {erreurAjout !== null && <MessageErreur erreur={erreurAjout} />}
              </div>
            </>
          )}
        </>
      )}
    </div>
  );
}
