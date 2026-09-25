import { useState } from "react";
import type { FormEvent } from "react";
import { marquerPresence } from "../api/presences";
import { MessageErreur } from "./MessageErreur";

export function MarquerPresence({
  etudiantId,
  onEnregistree,
}: {
  etudiantId: number;
  onEnregistree?: () => void;
}) {
  const [code, setCode] = useState("");
  const [envoiEnCours, setEnvoiEnCours] = useState(false);
  const [erreur, setErreur] = useState<unknown>(null);
  const [enregistree, setEnregistree] = useState(false);

  async function envoyer(e: FormEvent) {
    e.preventDefault();
    setEnvoiEnCours(true);
    setErreur(null);
    setEnregistree(false);
    try {
      await marquerPresence(code, etudiantId);
      setEnregistree(true);
      setCode("");
      onEnregistree?.();
    } catch (err) {
      setErreur(err);
    } finally {
      setEnvoiEnCours(false);
    }
  }

  return (
    <div>
      <h3>Marquer ma présence</h3>
      <form onSubmit={envoyer} className="form-code">
        <label htmlFor="code-presence">Code donné par le formateur</label>
        <input
          id="code-presence"
          className="saisie-code"
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          maxLength={6}
          autoComplete="off"
          autoCapitalize="characters"
          spellCheck={false}
        />
        <button type="submit" disabled={envoiEnCours || code.trim() === ""}>
          {envoiEnCours ? "Envoi..." : "Je suis présent"}
        </button>
      </form>
      {enregistree && (
        <p role="status" className="succes">
          Présence enregistrée.
        </p>
      )}
      {erreur !== null && <MessageErreur erreur={erreur} />}
    </div>
  );
}
