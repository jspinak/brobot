import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, Grid, Paper, Typography } from '@mui/material';
import StateDetailsButtons from './state-details-buttons.component';
import StateImageSet from './../../components/state-image-set/state-image-set.component';
import StateImageOptions from './../../components/state-image/state-image-options.component';
import Scene from './../../components/scene/scene.component';
import useStateImages from './../../hooks/useStateImages';
import useTransitions from './../../hooks/useTransitions';
import useScenes from './../../hooks/useScenes';
import api from './../../services/api';

const StateDetailsContainer = ({ allStates: initialAllStates = [], isLoading }) => {
  const { stateId } = useParams();
  const [state, setState] = useState(null);
  const [allStates, setAllStates] = useState(initialAllStates);
  const [stateName, setStateName] = useState('');
  const [highlightAll, setHighlightAll] = useState(true);
  const navigate = useNavigate();

  const {
    stateImages,
    selectedStateImage,
    handleStateImageClick,
    handleStateImageUpdate,
    handleStateImageNameChange,
    handleDeleteStateImage,
    handleMoveStateImage
  } = useStateImages(state, setAllStates, stateId);

  const {
    transitions,
    selectedTransition,
    handleTransitionSelect,
    handleTransitionUpdate,
    handleTransitionDelete,
    fetchTransitions
  } = useTransitions(stateId);

  const {
    scenes,
    highlightedPatterns,
    handleStateImageHover,
    handleStateImageLeave,
    handleSceneImageLoad,
    calculateScaledPosition
  } = useScenes(state);

  useEffect(() => {
    if (!isLoading && allStates && allStates.length > 0) {
      const parsedStateId = parseInt(stateId, 10);
      const foundState = allStates.find(s => s.id === parsedStateId);
      if (foundState) {
        setState(foundState);
        setStateName(foundState.name);
        fetchTransitions();
      } else {
        console.error('State not found');
      }
    }
  }, [stateId, allStates, isLoading, fetchTransitions]);

  const handleCheckboxChange = () => {
    setHighlightAll(!highlightAll);
  };

  const handleStateNameChange = (newName) => {
    setStateName(newName);
  };

  const handleStateNameSubmit = async () => {
    try {
      const response = await api.put(`/api/states/${state.id}/name`, { name: stateName });
      if (response.status === 200) {
        setState(prevState => ({ ...prevState, name: stateName }));
        setAllStates(prevAllStates =>
          prevAllStates.map(s => s.id === state.id ? { ...s, name: stateName } : s)
        );
      } else {
        console.error('Failed to update state name');
      }
    } catch (error) {
      console.error('Error updating state name:', error);
    }
  };

  const handleStateDeleted = (deletedStateId) => {
    setAllStates(prevStates => prevStates.filter(state => state.id !== deletedStateId));
    if (allStates.length > 1) {
      const nextState = allStates.find(state => state.id !== deletedStateId);
      if (nextState) {
        navigate(`/states/${nextState.id}`);
      } else {
        navigate('/');
      }
    } else {
      navigate('/');
    }
  };

  if (isLoading) return <div>Loading...</div>;
  if (!state) return <div>State not found</div>;

  const containerStyle = {
    height: '100%',
    overflowY: 'auto',
    overflowX: 'hidden',
    padding: '16px',
    '&::-webkit-scrollbar': {
      width: '8px',
    },
    '&::-webkit-scrollbar-track': {
      background: '#f1f1f1',
    },
    '&::-webkit-scrollbar-thumb': {
      background: '#888',
      borderRadius: '4px',
    },
    '&::-webkit-scrollbar-thumb:hover': {
      background: '#555',
    },
  };

  return (
    <Box className="state-details" sx={{ height: 'calc(100vh - 64px)', display: 'flex', flexDirection: 'column' }}>
      <StateDetailsButtons
        isChecked={highlightAll}
        onCheckboxChange={handleCheckboxChange}
        states={allStates}
        currentStateId={state.id}
        currentStateName={stateName}
        onStateNameChange={handleStateNameChange}
        onStateNameSubmit={handleStateNameSubmit}
        stateImageCount={stateImages.length}
        transitionCount={transitions.filter(t => t.sourceStateId === state.id).length}
        onStateDeleted={handleStateDeleted}
      />
<Grid container spacing={2} sx={{ flexGrow: 1, height: 'calc(100% - 50px)', overflow: 'hidden' }}>
  <Grid item xs={3} sx={{ height: '100%' }}>
    <Paper elevation={3} sx={{ height: '100%' }}>
      <Box sx={containerStyle}>
        <Typography variant="h6" gutterBottom>State Images</Typography>
        <StateImageSet
          stateImages={stateImages}
          transitions={transitions}
          onHover={handleStateImageHover}
          onMouseLeave={handleStateImageLeave}
          onClick={handleStateImageClick}
          stateImage={selectedStateImage}
          allStates={allStates}
          currentStateId={state.id}
          onTransitionSelect={handleTransitionSelect}
        />
      </Box>
    </Paper>
  </Grid>
  <Grid item xs={5} sx={{ height: '100%' }}>
    <Paper elevation={3} sx={{ height: '100%' }}>
      <Box sx={containerStyle}>
        {selectedStateImage ? (
          <StateImageOptions
            stateImage={selectedStateImage}
            allStates={allStates}
            transitions={transitions}
            onCreateTransition={fetchTransitions}
            onNameChange={handleStateImageNameChange}
            onDelete={handleDeleteStateImage}
            onMove={handleMoveStateImage}
            onStateImageUpdate={handleStateImageUpdate}
            currentStateId={state.id}
            selectedTransition={selectedTransition}
            onTransitionUpdate={handleTransitionUpdate}
            onTransitionDelete={handleTransitionDelete}
            fetchTransitions={fetchTransitions}
          />
        ) : (
          <Typography>No state image selected</Typography>
        )}
      </Box>
    </Paper>
  </Grid>
  <Grid item xs={4} sx={{ height: '100%' }}>
    <Paper elevation={3} sx={{ height: '100%' }}>
      <Box sx={containerStyle}>
        <Typography variant="h6" gutterBottom>Scenes</Typography>
        {scenes.map((scene) => (
          <Scene
            key={scene.id}
            sceneId={scene.id}
            highlightAll={highlightAll}
            state={state}
            highlightedPatterns={highlightedPatterns}
            handleImageLoad={handleSceneImageLoad}
            calculateScaledPosition={calculateScaledPosition}
          />
        ))}
      </Box>
    </Paper>
  </Grid>
</Grid>
</Box>
  );
};

export default StateDetailsContainer;