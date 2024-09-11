import React from 'react';
import ReactDOM from 'react-dom/client'; // Correct import for React 18
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import { BrowserRouter as Router } from 'react-router-dom';
import { ProjectProvider } from './components/ProjectContext';

// Use createRoot in React 18
const root = ReactDOM.createRoot(document.getElementById('root'));

root.render(
  <React.StrictMode>
    <Router>
      <ProjectProvider>
        <App />
      </ProjectProvider>
    </Router>
  </React.StrictMode>
);

reportWebVitals();
