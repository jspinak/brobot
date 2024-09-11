import React, { useState, useContext, useEffect     } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Box, TextField, Button, Typography, Container } from '@mui/material';
import useFetchImages from './../../hooks/useFetchImages';
import useFetchScenes from './../../hooks/useFetchScenes';
import NewStateDisplay from './new-state-display.component';
import PatternList from './../../components/pattern-list/pattern-list.component';
import { ProjectContext } from './../../components/ProjectContext';

const CreateState = () => {
  const { selectedProject } = useContext(ProjectContext);
  const navigate = useNavigate();
  const [stateName, setStateName] = useState('');
  const [bundlePath, setBundlePath] = useState('C:\\Users\\jspin\\Documents\\brobot_parent\\FloraNext\\website-images');
  const [loadedPatterns, setLoadedPatterns] = useState([]);
  const [selectedPatterns, setSelectedPatterns] = useState([]);
  const images = useFetchImages();
  const scenes = useFetchScenes();
  const [newState, setNewState] = useState(null);

  console.log('Selected Project:', selectedProject);

  useEffect(() => {
    if (newState) {
      console.log('New State:', newState);
    }
    console.log('CreateState: Current selected project:', selectedProject);
  }, [newState, selectedProject]);

    const handleLoadPatterns = async () => {
      try {
        const response = await axios.post('${process.env.REACT_APP_BROBOT_API_URL}/api/patterns/load-from-bundle', { bundlePath });
        console.log('Loaded patterns:', response.data);

        setLoadedPatterns(prevPatterns => {
          // Combine previous and new patterns
          const allPatterns = [...prevPatterns, ...response.data];

          // Remove duplicates based on id
          const uniquePatterns = Array.from(new Map(allPatterns.map(pattern => [pattern.id, pattern])).values());

          return uniquePatterns;
        });
      } catch (error) {
        console.error('Error loading patterns:', error);
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
      console.error('No project selected');
      return;
    }

    const newStateObj = {
      name: stateName,
      projectRequest: { id: selectedProject.id },
      stateImages: selectedPatterns.map(patternId => {
        const pattern = loadedPatterns.find(p => p.id === patternId);
        console.log('Creating state image from pattern:', pattern);
        return {
          patterns: [{
            id: pattern.id,
            name: pattern.name,
            image: pattern.image
          }]
        };
      }),
      transitions: [],
      canHide: [],
      hidden: [],
      pathScore: 1,
      probabilityExists: 100
    };

    console.log('Sending state object:', newStateObj);

    if (newStateObj.stateImages.length === 0) {
      console.error('No patterns selected');
      return;
    }

    try {
      const response = await axios.post(`${process.env.REACT_APP_BROBOT_API_URL}/api/states`, newStateObj, {
        headers: {
          'Content-Type': 'application/json',
        },
      });
      console.log('Server response:', JSON.stringify(response.data, null, 2));
      setNewState(response.data);
      // Don't navigate away, so we can display the new state
      // navigate('/home');
    } catch (error) {
      console.error('Error saving new state:', error);
      if (error.response) {
        console.error('Server error response:', error.response.data);
      }
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
          <Button variant="contained" onClick={handleLoadPatterns}>
            Load Patterns
          </Button>
        </Box>
        <Button variant="contained" color="primary" onClick={handleCreateState} sx={{ mt: 2 }}>
          Create State
        </Button>
        {newState && <NewStateDisplay newState={newState} />}
      </Box>
      <PatternList patterns={loadedPatterns} selectedPatterns={selectedPatterns} onPatternSelect={handlePatternSelect} />
    </Container>
  );
};

export default CreateState;