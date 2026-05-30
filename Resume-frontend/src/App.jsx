import React, { useState, useEffect } from "react";

import LandingPage    from "./pages/LandingPage";
import Dashboard     from "./pages/Dashboard";
import LoginPage     from "./pages/LoginPage";
import RegisterPage  from "./pages/RegisterPage";
import ResultsPage   from "./pages/ResultsPage";
import AIHub         from "./pages/AIHub";
import AnalyticsPage from "./pages/AnalyticsPage";
import SettingsPage  from "./pages/SettingsPage";

function App() {
  const [currentView, setCurrentView]     = useState(() => {
    const path = window.location.pathname;
    if (path === '/login')     return 'login';
    if (path === '/register')  return 'register';
    if (path === '/dashboard') return 'dashboard';
    if (path === '/aihub')     return 'aihub';
    if (path === '/results')   return 'results';
    if (path === '/analytics') return 'analytics';
    if (path === '/settings')  return 'settings';
    return 'landing';
  });
  const [currentResume, setCurrentResume] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [resumes, setResumes]             = useState([]);   // lifted so AIHub can access them

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setIsAuthenticated(true);
      const lastView = sessionStorage.getItem('lastView');
      if (['dashboard','results','aihub','analytics','settings'].includes(lastView)) {
        setCurrentView(lastView);
      } else {
        setCurrentView('dashboard');
      }
    } else {
      // If not authenticated, ensure private routes go to landing
      const path = window.location.pathname;
      const privateRoutes = ['/dashboard','/aihub','/results','/analytics','/settings'];
      if (privateRoutes.includes(path)) {
        setCurrentView('landing');
      }
    }
  }, []);

  useEffect(() => {
    if (currentView === 'landing') {
      window.history.replaceState({}, '', '/');
    } else {
      window.history.replaceState({}, '', `/${currentView}`);
      sessionStorage.setItem('lastView', currentView);
    }
  }, [currentView]);

  const handleViewChange = (view) => {
    setCurrentView(view);
  };

  const handleLogin = () => {
    setIsAuthenticated(true);
    setCurrentView('dashboard');
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    sessionStorage.removeItem('lastView');
    setIsAuthenticated(false);
    setCurrentResume(null);
    setResumes([]);
    setCurrentView('landing');
  };

  const renderView = () => {
    switch (currentView) {
      case 'landing':
        return (
          <LandingPage
            setCurrentView={handleViewChange}
            setCurrentResume={setCurrentResume}
          />
        );

      case 'login':
        return (
          <LoginPage
            setCurrentView={handleViewChange}
            onLoginSuccess={handleLogin}
          />
        );

      case 'register':
        return (
          <RegisterPage
            setCurrentView={handleViewChange}
            onRegisterSuccess={handleLogin}
          />
        );

      case 'dashboard':
        return (
          <Dashboard
            setCurrentView={handleViewChange}
            setCurrentResume={setCurrentResume}
            onLogout={handleLogout}
            onResumesLoaded={setResumes}
          />
        );

      case 'results':
        return (
          <ResultsPage
            setCurrentView={handleViewChange}
            currentResume={currentResume}
          />
        );

      case 'aihub':
        return (
          <AIHub
            setCurrentView={handleViewChange}
            resumes={resumes}
          />
        );

      case 'analytics':
        return (
          <AnalyticsPage
            setCurrentView={handleViewChange}
            onLogout={handleLogout}
          />
        );

      case 'settings':
        return (
          <SettingsPage
            setCurrentView={handleViewChange}
            onLogout={handleLogout}
          />
        );

      default:
        return (
          <LandingPage
            setCurrentView={handleViewChange}
            setCurrentResume={setCurrentResume}
          />
        );
    }
  };

  return (
    <div className="App">
      {renderView()}
    </div>
  );
}

export default App;