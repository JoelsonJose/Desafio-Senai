import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App.jsx';
import { ToastProvider } from './components/ui/ToastProvider';
import { LeagueProvider } from './context/LeagueContext';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <ToastProvider>
        <LeagueProvider>
          <App />
        </LeagueProvider>
      </ToastProvider>
    </BrowserRouter>
  </React.StrictMode>,
);
