import React, { useState, useEffect, useContext } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import axios from 'axios';
import NavigationBar from './routes/navigation/navigation.component';
import Home from './routes/home/home.component';
import StateDetails from './routes/state/state-details.component';
import CreateState from './routes/create-state/create-state.component';
import AllStates from './routes/view-states/all-states.component';
import { ProjectContext } from './components/ProjectContext';

function App() {
  const [states, setStates] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const { selectedProject } = useContext(ProjectContext);

  useEffect(() => {
    console.log('App: Selected Project changed:', selectedProject);
    if (!selectedProject) {
      console.log('App: No project selected, clearing states');
      setStates([]);
      setIsLoading(false);
      return;
    }

    const fetchStates = async () => {
      setIsLoading(true);
      try {
        console.log(`App: Fetching states for project ${selectedProject.id}`);
        const response = await axios.get(`http://localhost:8080/api/states/project/${selectedProject.id}`);
        console.log('App: Fetched states:', response.data);
        setStates(response.data);
      } catch (error) {
        console.error('App: Error fetching states:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchStates();
  }, [selectedProject]);

  console.log('App: Rendering with states:', states);

  return (
    <>
      <NavigationBar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/states/*" element={<AllStates states={states} isLoading={isLoading} />} />
        <Route path="/states/:stateId" element={<StateDetails allStates={states} isLoading={isLoading} />} />
        <Route path="/create-state" element={<CreateState />} />
      </Routes>
    </>
  );
}

export default App;
