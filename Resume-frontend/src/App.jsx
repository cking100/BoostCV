import React, { useState, useEffect } from "react";

import LandingPage from "./pages/LandingPage";
import Dashboard from "./pages/Dashboard";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ResultsPage from "./pages/ResultsPage";


function App() {
  const [currentView, setCurrentView] = useState('landing');
  const [currentResume, setCurrentResume] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if(token){
      setIsAuthenticated(true);
      const lastView = sessionStorage.getItem('lastView');
      if(lastView === 'dashboard' || lastView === 'results'){
        setCurrentView(lastView);
      }else{
        setCurrentView('dashboard');
      }
    }
  }, []);

  useEffect(() => {
    if(currentView !== 'landing'){
      sessionStorage.setItem('lastView', currentView);
    }
  }, [currentView]);

  const handleViewChange = (view) => {
    console.log('Changing view to:', view);
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
    setCurrentView('landing');
  };

  const renderView = () => {
    console.log('Rendering view:', currentView);
    
    switch(currentView){
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
          />
        );
      
      case 'results':
        return (
          <ResultsPage 
            setCurrentView={handleViewChange}
            resume={currentResume}
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