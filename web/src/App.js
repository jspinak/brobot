import React, { useState, useEffect } from 'react';
import { Routes, Route } from 'react-router-dom';
import NavigationBar from './routes/navigation/navigation.component';
import Home from './routes/home/home.component';
import AllStates from './routes/view-states/all-states.component';
import StateDetails from './routes/state/state-details.component';
import CreateState from './routes/create-state/create-state.component';

function App() {
  const [states, setStates] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetch('http://localhost:8080/api/states/all')
      .then(response => response.json())
      .then(data => {
        setStates(data);
        setIsLoading(false);
      })
      .catch(error => {
        console.error('Error fetching states:', error);
        setIsLoading(false);
      });
  }, []);

  return (
    <>
      <NavigationBar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/states/*" element={<AllStates states={states} isLoading={isLoading} />} />
        <Route path="/states/:stateId" element={<StateDetails allStates={states} />} />
        <Route path="/create-state" element={<CreateState />} />
      </Routes>
    </>
  );
}

export default App;