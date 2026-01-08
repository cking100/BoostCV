import React, { useState } from "react";
import "./App.css";
import LandingPage from "./pages/LandingPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import Dashboard from "./pages/Dashboard";
import ResultsPage from "./pages/ResultsPage";

function App() {
  const [currentView, setCurrentView] = useState("landing");

  // ✅ Added resume state
  const [currentResume, setCurrentResume] = useState(null);

  const renderView = () => {
    switch (currentView) {
      case "landing":
        return (
          <LandingPage
            setCurrentView={setCurrentView}
            setCurrentResume={setCurrentResume}
          />
        );

      case "login":
        return <LoginPage setCurrentView={setCurrentView} />;

      case "register":
        return <RegisterPage setCurrentView={setCurrentView} />;

      case "dashboard":
        return (
          <Dashboard
            setCurrentView={setCurrentView}
            setCurrentResume={setCurrentResume}
          />
        );

      case "results":
        return (
          <ResultsPage
            setCurrentView={setCurrentView}
            currentResume={currentResume}
          />
        );

      default:
        return (
          <LandingPage
            setCurrentView={setCurrentView}
            setCurrentResume={setCurrentResume}
          />
        );
    }
  };

  return <div className="app">{renderView()}</div>;
}

export default App;
