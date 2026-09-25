import { BrowserRouter, Link, Route, Routes } from "react-router-dom";
import { IdentiteProvider } from "./identite/IdentiteContext";
import EtudiantPage from "./pages/EtudiantPage";
import FormateurPage from "./pages/FormateurPage";

function Accueil() {
  return (
    <section>
      <p>Choisissez votre espace :</p>
      <ul>
        <li>
          <Link to="/formateur">Formateur</Link>
        </li>
        <li>
          <Link to="/etudiant">Étudiant</Link>
        </li>
      </ul>
    </section>
  );
}

export default function App() {
  return (
    <IdentiteProvider>
      <BrowserRouter>
        <header className="entete">
          <nav>
            <Link to="/">Accueil</Link> · <Link to="/formateur">Formateur</Link>{" "}
            · <Link to="/etudiant">Étudiant</Link>
          </nav>
        </header>
        <main>
          <h1>KF48 Présences & Relectures</h1>
          <Routes>
            <Route path="/" element={<Accueil />} />
            <Route path="/formateur" element={<FormateurPage />} />
            <Route path="/etudiant" element={<EtudiantPage />} />
          </Routes>
        </main>
      </BrowserRouter>
    </IdentiteProvider>
  );
}
