import { BrowserRouter, Link, Route, Routes } from "react-router-dom";
import FormateurPage from "./pages/FormateurPage";

function Accueil() {
  return (
    <section>
      <p>Choisissez votre espace :</p>
      <ul>
        <li>
          <Link to="/formateur">Formateur</Link>
        </li>
      </ul>
    </section>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <header className="entete">
        <nav>
          <Link to="/">Accueil</Link> · <Link to="/formateur">Formateur</Link>
        </nav>
      </header>
      <main>
        <h1>KF48 Présences & Relectures</h1>
        <Routes>
          <Route path="/" element={<Accueil />} />
          <Route path="/formateur" element={<FormateurPage />} />
        </Routes>
      </main>
    </BrowserRouter>
  );
}
