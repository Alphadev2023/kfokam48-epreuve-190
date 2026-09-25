import { useState } from "react";
import type { FormEvent } from "react";
import { rendreRelecture } from "../api/relectures";
import type { RelectureAFaire } from "../api/types";
import { MessageErreur } from "./MessageErreur";

interface Props {
  relecture: RelectureAFaire;
  etudiantId: number;
  onRendue: () => void;
}

/** Aucune règle métier dupliquée (F3) : la validité de la note est décidée par l'API. */
export function FormulaireRelecture({
  relecture,
  etudiantId,
  onRendue,
}: Props) {
  const [note, setNote] = useState("");
  const [commentaire, setCommentaire] = useState("");
  const [envoiEnCours, setEnvoiEnCours] = useState(false);
  const [erreur, setErreur] = useState<unknown>(null);

  async function envoyer(e: FormEvent) {
    e.preventDefault();
    setEnvoiEnCours(true);
    setErreur(null);
    try {
      await rendreRelecture(
        relecture.id,
        Number(note),
        commentaire,
        etudiantId,
      );
      onRendue();
    } catch (err) {
      setErreur(err);
    } finally {
      setEnvoiEnCours(false);
    }
  }

  return (
    <form onSubmit={envoyer} className="form-relecture">
      <label>
        Note sur 20{" "}
        <input
          type="number"
          inputMode="numeric"
          min={0}
          max={20}
          step={1}
          value={note}
          onChange={(e) => setNote(e.target.value)}
        />
      </label>
      <label>
        Commentaire
        <textarea
          rows={4}
          maxLength={2000}
          value={commentaire}
          onChange={(e) => setCommentaire(e.target.value)}
        />
      </label>
      <p>Attention : une relecture envoyée est définitive.</p>
      <button
        type="submit"
        disabled={
          envoiEnCours || note.trim() === "" || commentaire.trim() === ""
        }
      >
        {envoiEnCours ? "Envoi..." : "Envoyer la relecture"}
      </button>
      {erreur !== null && <MessageErreur erreur={erreur} />}
    </form>
  );
}
