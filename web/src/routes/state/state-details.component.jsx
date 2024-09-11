import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Box, Grid, Paper, Typography } from '@mui/material';
import StateImageSet from './../../components/state-image-set/state-image-set.component';
import Scene from './../../components/scene/scene.component';
import StateDetailsButtons from "./state-details-buttons.component";
import StateImageOptions from './../../components/state-image/state-image-options.component';
import StateImage from './../../components/state-image/state-image.component';
import './state-details.styles.css';

const StateDetails = ({ allStates: initialAllStates = [], isLoading }) => {
  const [state, setState] = useState(null);
  const [selectedStateImage, setSelectedStateImage] = useState(null);
  const [highlightedPatterns, setHighlightedPatterns] = useState([]);
  const [imageDimensions, setImageDimensions] = useState({});
  const [selectedStateImages, setSelectedStateImages] = useState([]);
  const [isEditing, setIsEditing] = useState(false);
  const { stateId } = useParams();
  const [highlightAll, setHighlightAll] = useState(true);
  const [transitions, setTransitions] = useState([]);
  const [selectedTargetState, setSelectedTargetState] = useState('');
  const [stateName, setStateName] = useState('');
  const [allStates, setAllStates] = useState(initialAllStates);
  const navigate = useNavigate();
  const [selectedTransition, setSelectedTransition] = useState(null);

    const containerStyle = {
      height: '100%',
      overflowY: 'scroll',
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

  const handleStateImageUpdate = useCallback((stateImageId, newStateId) => {
    console.log('handleStateImageUpdate called with:', { stateImageId, newStateId });

    setAllStates(prevAllStates => {
      console.log('Previous allStates:', prevAllStates);

      const sourceState = prevAllStates.find(s => s.id === parseInt(stateId));
      const destState = prevAllStates.find(s => s.id === parseInt(newStateId));

      if (!sourceState || !destState) {
        console.error('Source or destination state not found');
        return prevAllStates;
      }

      const imageToMove = sourceState.stateImages.find(img => img.id === stateImageId);

      if (!imageToMove) {
        console.error('Image to move not found');
        return prevAllStates;
      }

      const updatedStates = prevAllStates.map(stateItem => {
        if (stateItem.id === parseInt(stateId)) {
          return {
            ...stateItem,
            stateImages: stateItem.stateImages.filter(img => img.id !== stateImageId)
          };
        }
        if (stateItem.id === parseInt(newStateId)) {
          return {
            ...stateItem,
            stateImages: [...stateItem.stateImages, imageToMove]
          };
        }
        return stateItem;
      });

      console.log('Updated allStates:', updatedStates);
      return updatedStates;
    });

    setState(prevState => {
      if (prevState.id === parseInt(stateId)) {
        console.log('Updating current state');
        return {
          ...prevState,
          stateImages: prevState.stateImages.filter(img => img.id !== stateImageId)
        };
      }
      if (prevState.id === parseInt(newStateId)) {
        console.log('Updating new state');
        const imageToMove = allStates.find(s => s.id === parseInt(stateId))?.stateImages.find(img => img.id === stateImageId);
        return {
          ...prevState,
          stateImages: [...prevState.stateImages, imageToMove]
        };
      }
      return prevState;
    });
  }, [stateId, allStates]);

  const handleMoveStateImage = useCallback(async (stateImageId, newStateId) => {
    console.log('handleMoveStateImage called with:', { stateImageId, newStateId });
    try {
      const response = await axios.put(`${process.env.REACT_APP_BROBOT_API_URL}/api/states/move-image`, {
        stateImageId,
        newStateId
      });

      if (response.status === 200) {
        console.log('API call successful, response:', response.data);
        handleStateImageUpdate(stateImageId, newStateId);
        console.log('State image moved successfully');
      } else {
        console.error('Failed to move state image, status:', response.status);
      }
    } catch (error) {
      console.error('Error moving state image:', error);
    }
  }, [handleStateImageUpdate]);

  const fetchTransitions = useCallback(async () => {
    try {
      const response = await fetch('${process.env.REACT_APP_BROBOT_API_URL}/api/transitions');
      if (response.ok) {
        const data = await response.json();
        setTransitions(data);
      } else {
        console.error('Failed to fetch transitions');
      }
    } catch (error) {
      console.error('Error fetching transitions:', error);
    }
  }, []);

  useEffect(() => {
    if (!isLoading && allStates && allStates.length > 0) {
      console.log('StateId from URL:', stateId);
      console.log('All States:', allStates);

      const parsedStateId = parseInt(stateId, 10);
      const foundState = allStates.find(s => s.id === parsedStateId);

      console.log('Parsed StateId:', parsedStateId);
      console.log('Found State:', foundState);

      if (foundState) {
        setState(foundState);
        fetchTransitions();
      } else {
        console.error('State not found');
      }
    }
  }, [stateId, allStates, isLoading, fetchTransitions]);

  const handleCheckboxChange = () => {
    setHighlightAll(!highlightAll);
  };

  const handleStateImageHover = useCallback((stateImage) => {
    if (stateImage && Array.isArray(stateImage.patterns)) {
      setHighlightedPatterns(stateImage.patterns);
    } else {
      console.error('Invalid stateImage or patterns:', stateImage);
    }
  }, []);

  const handleStateImageLeave = useCallback(() => {
    setHighlightedPatterns([]);
  }, []);

  const handleStateImageClick = useCallback((stateImage) => {
    setSelectedStateImage(stateImage);
  }, []);

  const handleStateImageCheckboxChange = useCallback((stateImageId) => {
    setSelectedStateImages((prevSelected) =>
        prevSelected.includes(stateImageId)
            ? prevSelected.filter(id => id !== stateImageId)
            : [...prevSelected, stateImageId]
    );
  }, []);

  const handleEditToggle = useCallback(() => {
    setIsEditing((prevIsEditing) => !prevIsEditing);
  }, []);

  const handleImageLoad = useCallback((event, sceneId) => {
    setImageDimensions(prevDimensions => ({
      ...prevDimensions,
      [sceneId]: {
        width: event.target.width,
        height: event.target.height
      }
    }));
  }, []);

    const handleStateDeleted = (deletedStateId) => {
      setAllStates(prevStates => prevStates.filter(state => state.id !== deletedStateId));
      if (allStates && allStates.length > 1) {
        navigate(`/states/${allStates[0].id}`);
      } else {
        navigate('/');
      }
    };

  const handleDeleteImages = useCallback(async () => {
    try {
      const response = await fetch('${process.env.REACT_APP_BROBOT_API_URL}/api/state-images/delete', {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ stateImageIds: selectedStateImages }),
      });

      if (response.ok) {
        setState((prevState) => ({
          ...prevState,
          stateImages: prevState.stateImages.filter((stateImage) =>
              !selectedStateImages.includes(stateImage.id)
          ),
        }));
        setSelectedStateImages([]);
      } else {
        console.error('Failed to delete state images');
      }
    } catch (error) {
      console.error('Error deleting state images:', error);
    }
  }, [selectedStateImages]);

  const handleStateImageNameChange = useCallback((stateImageId, newName) => {
    setState(prevState => ({
      ...prevState,
      stateImages: prevState.stateImages.map(stateImage =>
          stateImage.id === stateImageId
              ? { ...stateImage, name: newName }
              : stateImage
      )
    }));
  }, []);

  const handleCreateTransition = useCallback(async (newTransition, updatedStateImage) => {
    // Update the state image
    if (updatedStateImage) {
      setState(prevState => ({
        ...prevState,
        stateImages: prevState.stateImages.map(si =>
            si.id === updatedStateImage.id ? updatedStateImage : si
        )
      }));
    }

    // Fetch all transitions again to ensure we have the latest data
    await fetchTransitions();
  }, [fetchTransitions]);

  const handleDeleteStateImage = useCallback(async (stateImageId) => {
    try {
      const response = await fetch(`${process.env.REACT_APP_BROBOT_API_URL}/api/stateimages/${stateImageId}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        setState(prevState => ({
          ...prevState,
          stateImages: prevState.stateImages.filter(si => si.id !== stateImageId)
        }));
        setSelectedStateImage(null);
      } else {
        console.error('Failed to delete state image');
      }
    } catch (error) {
      console.error('Error deleting state image:', error);
    }
  }, []);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!state) {
    return <div>State not found</div>;
  }

  const calculateScaledPosition = (pattern, sceneId) => {
    const dimensions = imageDimensions[sceneId] || { width: 1920, height: 1080 };
    const scaleW = dimensions.width / 1920;
    const scaleH = dimensions.height / 1080;
    const adjustX = 10;
    const adjustY = 10;
    return { adjustX, adjustY, scaleW, scaleH };
  };

  const handleStateNameChange = (newName) => {
    setStateName(newName);
  };

  const handleStateNameSubmit = async () => {
    try {
      const response = await axios.put(`${process.env.REACT_APP_BROBOT_API_URL}/api/states/${state.id}/name`, { name: stateName });
      if (response.status === 200) {
        console.log('State name updated successfully');
        // Update the local state
        setState(prevState => ({ ...prevState, name: stateName }));

        // Update the allStates array
        setAllStates(prevAllStates =>
            prevAllStates.map(s =>
                s.id === state.id ? { ...s, name: stateName } : s
            )
        );
      } else {
        console.error('Failed to update state name');
      }
    } catch (error) {
      console.error('Error updating state name:', error);
    }
  };

  const handleTransitionSelect = (transition) => {
    setSelectedTransition(transition);
  };

  const handleTransitionUpdate = async (updatedTransition) => {
    try {
      const response = await axios.put(`${process.env.REACT_APP_BROBOT_API_URL}/api/transitions/${updatedTransition.id}`, updatedTransition);
      if (response.status === 200) {
        setTransitions(prevTransitions =>
          prevTransitions.map(t => t.id === updatedTransition.id ? updatedTransition : t)
        );
      }
    } catch (error) {
      console.error('Error updating transition:', error);
    }
  };

  const handleTransitionDelete = async (transitionId) => {
    try {
      const response = await axios.delete(`${process.env.REACT_APP_BROBOT_API_URL}/api/transitions/${transitionId}`);
      if (response.status === 204) {
        setTransitions(prevTransitions => prevTransitions.filter(t => t.id !== transitionId));
        setSelectedTransition(null);
      }
    } catch (error) {
      console.error('Error deleting transition:', error);
    }
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!state) {
    return <div>State not found</div>;
  }

  return (
    <Box className="state-details" sx={{ height: 'calc(100vh - 64px)', display: 'flex', flexDirection: 'column' }}>
      <StateDetailsButtons
            isChecked={highlightAll}
            onCheckboxChange={handleCheckboxChange}
            states={allStates}
            currentStateId={state?.id}
            currentStateName={stateName}
            onStateNameChange={handleStateNameChange}
            onStateNameSubmit={handleStateNameSubmit}
            stateImageCount={state?.stateImages?.length || 0}
            transitionCount={transitions.filter(t => t.sourceStateId === state?.id).length}
            onStateDeleted={handleStateDeleted}
      />
      <Grid container spacing={2} sx={{ flexGrow: 1, height: 'calc(100% - 50px)', overflow: 'hidden' }}>
              <Grid item xs={3} sx={{ height: '100%' }}>
                <Paper elevation={3} sx={{ height: '100%' }}>
                  <Box sx={containerStyle}>
                    <Typography variant="h6" gutterBottom>State Images</Typography>
                    <StateImageSet
                      stateImages={state?.stateImages || []}
                      transitions={transitions}
                      onHover={handleStateImageHover}
                      onMouseLeave={handleStateImageLeave}
                      onClick={handleStateImageClick}
                      selectedStateImage={selectedStateImage}
                      allStates={allStates}
                      currentStateId={state?.id}
                      onTransitionSelect={handleTransitionSelect}
                    />
                  </Box>
                </Paper>
              </Grid>
              <Grid item xs={5} sx={{ height: '100%' }}>
                <Paper elevation={3} sx={{ height: '100%' }}>
                  <Box sx={containerStyle}>
                    <Typography variant="h6" gutterBottom>Options</Typography>
                    {selectedStateImage ? (
                      <StateImageOptions
                        stateImage={selectedStateImage}
                        allStates={allStates}
                        transitions={transitions}
                        onCreateTransition={handleCreateTransition}
                        onNameChange={handleStateImageNameChange}
                        onDelete={handleDeleteStateImage}
                        onMove={handleMoveStateImage}
                        onStateImageUpdate={handleStateImageUpdate}
                        currentStateId={state?.id}
                        selectedTransition={selectedTransition}
                        onTransitionUpdate={handleTransitionUpdate}
                        onTransitionDelete={handleTransitionDelete}
                      />
                    ) : (
                      <Typography>Select a state image to view options</Typography>
                    )}
                  </Box>
                </Paper>
              </Grid>
              <Grid item xs={4} sx={{ height: '100%' }}>
                <Paper elevation={3} sx={{ height: '100%' }}>
                  <Box sx={containerStyle}>
                    <Typography variant="h6" gutterBottom>Scenes</Typography>
                    {state.scenes && state.scenes.map((scene, index) => (
                      <Scene
                        key={scene.id}
                        sceneId={scene.id}
                        highlightAll={highlightAll}
                        state={state}
                        highlightedPatterns={highlightedPatterns}
                        handleImageLoad={handleImageLoad}
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

      export default StateDetails;