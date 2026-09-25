import { ChoixEtudiant } from "../components/ChoixEtudiant";
import { useIdentite } from "../identite/IdentiteContext";

export default function EtudiantPage() {
  const { identite, oublier } = useIdentite();

  if (!identite) {
    return (
      <section>
        <h2>Espace étudiant</h2>
        <p>Choisissez votre promotion, puis votre nom.</p>
        <ChoixEtudiant />
      </section>
    );
  }

  return (
    <section>
      <h2>Espace étudiant</h2>
      <p>
        Vous êtes <strong>{identite.nom}</strong>.{" "}
        <button type="button" onClick={oublier}>
          Ce n'est pas moi
        </button>
      </p>
    </section>
  );
}
