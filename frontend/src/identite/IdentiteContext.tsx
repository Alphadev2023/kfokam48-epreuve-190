import { createContext, useContext, useEffect, useState } from "react";
import type { ReactNode } from "react";

/** Q1 : pas de mot de passe, l'étudiant se choisit dans la liste. L'identité reste le temps de l'onglet. */
export interface Identite {
  etudiantId: number;
  nom: string;
  promotionId: number;
}

interface ValeurIdentite {
  identite: Identite | null;
  choisir: (identite: Identite) => void;
  oublier: () => void;
}

const CLE_STOCKAGE = "kf48-identite";

const ContexteIdentite = createContext<ValeurIdentite | null>(null);

function lireIdentite(): Identite | null {
  try {
    const brut = sessionStorage.getItem(CLE_STOCKAGE);
    return brut ? (JSON.parse(brut) as Identite) : null;
  } catch {
    return null;
  }
}

export function IdentiteProvider({ children }: { children: ReactNode }) {
  const [identite, setIdentite] = useState<Identite | null>(lireIdentite);

  useEffect(() => {
    if (identite)
      sessionStorage.setItem(CLE_STOCKAGE, JSON.stringify(identite));
    else sessionStorage.removeItem(CLE_STOCKAGE);
  }, [identite]);

  return (
    <ContexteIdentite.Provider
      value={{
        identite,
        choisir: setIdentite,
        oublier: () => setIdentite(null),
      }}
    >
      {children}
    </ContexteIdentite.Provider>
  );
}

export function useIdentite(): ValeurIdentite {
  const valeur = useContext(ContexteIdentite);
  if (!valeur)
    throw new Error("useIdentite doit être utilisé dans IdentiteProvider");
  return valeur;
}
