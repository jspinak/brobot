import React, { useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, TextField, Button, Typography, Container } from '@mui/material';
import useFetchImages from './../../hooks/useFetchImages';
import useFetchScenes from './../../hooks/useFetchScenes';
import NewStateDisplay from './new-state-display.component';
import PatternList from './../../components/pattern-list/pattern-list.component';
import { ProjectContext } from './../../components/ProjectContext';
import api from './../../services/api';

const CreateState = () => {
  const { selectedProject } = useContext(ProjectContext);
  const navigate = useNavigate();
  const [stateName, setStateName] = useState('');
  const [bundlePath, setBundlePath] = useState('C:\\Users\\jspin\\Documents\\brobot_parent\\FloraNext\\website-images');
  const [loadedPatterns, setLoadedPatterns] = useState([]);
  const [selectedPatterns, setSelectedPatterns] = useState([]);
  const [newState, setNewState] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  console.log('Selected Project:', selectedProject);

  useEffect(() => {
    if (newState) {
      console.log('New State:', newState);
    }
    console.log('CreateState: Current selected project:', selectedProject);
  }, [newState, selectedProject]);

const handleLoadPatterns = async () => {
  setLoading(true);
  setError(null);
  try {
    const response = await api.post(`/api/patterns/load-from-bundle`, { bundlePath });
    console.log('Loaded patterns:', response.data);

    setLoadedPatterns(prevPatterns => {
      const allPatterns = [...prevPatterns, ...response.data];
      const uniquePatterns = Array.from(new Map(allPatterns.map(pattern => [pattern.id, pattern])).values());
      console.log('Unique patterns:', uniquePatterns);
      return uniquePatterns;
    });
  } catch (error) {
    console.error('Error loading patterns:', error);
    setError('Failed to load patterns. Please try again.');
  } finally {
    setLoading(false);
  }
};

  const handlePatternSelect = (patternId) => {
    setSelectedPatterns(prev =>
      prev.includes(patternId)
        ? prev.filter(id => id !== patternId)
        : [...prev, patternId]
    );
  };

const handleCreateState = async () => {
  if (!selectedProject) {
    setError('No project selected');
    return;
  }

  if (selectedPatterns.length === 0) {
    setError('No patterns selected');
    return;
  }

  setLoading(true);
  setError(null);

  const newStateObj = {
    name: stateName,
    projectRequest: { id: selectedProject.id },
    stateImages: selectedPatterns.map(patternId => ({
      patterns: [{ id: patternId }]
    })),
    transitions: [],
    canHide: [],
    hidden: [],
    pathScore: 1,
    probabilityExists: 100
  };

  console.log('Sending state object:', newStateObj);

  try {
    const response = await api.post(`/api/states`, newStateObj, {
      headers: {
        'Content-Type': 'application/json',
      },
    });
    console.log('Server response:', JSON.stringify(response.data, null, 2));
    setNewState(response.data);
  } catch (error) {
    console.error('Error saving new state:', error);
    if (error.response) {
      console.error('Server error response:', error.response.data);
      setError(`Failed to create state: ${error.response.data.message || 'Unknown error'}`);
    } else {
      setError('Failed to create state. Please try again.');
    }
  } finally {
    setLoading(false);
  }
};

  return (
    <Container maxWidth="md">
      <Box sx={{ my: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Create New State
        </Typography>
        <TextField
          fullWidth
          label="State Name"
          value={stateName}
          onChange={(e) => setStateName(e.target.value)}
          margin="normal"
        />
        <Box sx={{ display: 'flex', gap: 2, my: 2 }}>
          <TextField
            fullWidth
            label="Bundle Path"
            value={bundlePath}
            onChange={(e) => setBundlePath(e.target.value)}
          />
          <Button variant="contained" onClick={handleLoadPatterns} disabled={loading}>
            {loading ? 'Loading...' : 'Load Patterns'}
          </Button>
        </Box>
        <Button
          variant="contained"
          color="primary"
          onClick={handleCreateState}
          disabled={loading || selectedPatterns.length === 0}
          sx={{ mt: 2 }}
        >
          {loading ? 'Creating...' : 'Create State'}
        </Button>
        {error && (
          <Typography color="error" sx={{ mt: 2 }}>
            {error}
          </Typography>
        )}
        {newState && <NewStateDisplay newState={newState} />}
      </Box>
      <PatternList
        patterns={loadedPatterns}
        selectedPatterns={selectedPatterns}
        onPatternSelect={handlePatternSelect}
      />
    </Container>
  );
};

export default CreateState;