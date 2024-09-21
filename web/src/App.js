import React, { useState, useEffect, useContext } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import NavigationBar from './routes/navigation/navigation.component';
import Home from './routes/home/home.component';
import StateDetailsContainer from './routes/state/StateDetailsContainer';
import CreateState from './routes/create-state/create-state.component';
import AllStates from './routes/view-states/all-states.component';
import TransitionGraph from './routes/transition-graph/TransitionGraph';
import { ProjectContext } from './components/ProjectContext';
import api from './services/api'

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
        const response = await api.get(`/api/states/project/${selectedProject.id}`);
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
        <Route path="/states/:stateId" element={<StateDetailsContainer allStates={states} isLoading={isLoading} />} />
        <Route path="/create-state" element={<CreateState />} />
        <Route
          path="/transition-graph"
          element={
            <TransitionGraph
              states={states}
              isLoading={isLoading}
              selectedProject={selectedProject}
            />
          }
        />
      </Routes>
    </>
  );
}

export default App;