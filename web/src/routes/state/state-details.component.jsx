import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import StateImageSet from './../../components/state-image-set/state-image-set.component';
import Scene from './../../components/scene/scene.component';
import StateDetailsButtons from "./state-details-buttons.component";
import './state-details.styles.css';

const StateDetails = ({ allStates: initialAllStates, isLoading }) => {
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

  const fetchTransitions = useCallback(async () => {
    try {
      const response = await fetch('http://localhost:8080/api/transitions');
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
    if (!isLoading && allStates.length > 0) {
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

  const handleDeleteImages = useCallback(async () => {
    try {
      const response = await fetch('http://localhost:8080/api/state-images/delete', {
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

  const handleStateImageUpdate = useCallback((updatedStateImage) => {
    setState(prevState => ({
      ...prevState,
      stateImages: prevState.stateImages.map(stateImage =>
          stateImage.id === updatedStateImage.id ? updatedStateImage : stateImage
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
      const response = await fetch(`http://localhost:8080/api/stateimages/${stateImageId}`, {
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

  const handleMoveStateImage = useCallback(async (stateImageId, newStateId) => {
    try {
      const response = await fetch(`http://localhost:8080/api/stateimages/${stateImageId}/move`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ newStateId }),
      });

      if (response.ok) {
        const updatedStateImage = await response.json();
        setState(prevState => ({
          ...prevState,
          stateImages: prevState.stateImages.filter(si => si.id !== stateImageId)
        }));
        setSelectedStateImage(null);
      } else {
        console.error('Failed to move state image');
      }
    } catch (error) {
      console.error('Error moving state image:', error);
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
      const response = await axios.put(`http://localhost:8080/api/states/${state.id}/name`, { name: stateName });
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

  return (
      <div className="state-details">
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
        />
        <div className="state-content">
          <div className="state-image-set">
            <StateImageSet
                stateImages={state?.stateImages || []}
                transitions={transitions}
                onHover={handleStateImageHover}
                onMouseLeave={handleStateImageLeave}
                onClick={handleStateImageClick}
                selectedStateImage={selectedStateImage}
                allStates={allStates}
                onCreateTransition={handleCreateTransition}
                onNameChange={handleStateImageNameChange}
                onDelete={handleDeleteStateImage}
                onMove={handleMoveStateImage}
                onStateImageUpdate={handleStateImageUpdate}
            />
          </div>
            <div className="state-scenes">
              {state.scenes && state.scenes.map((scene, index) => {
                if (!scene || typeof scene !== 'object') {
                  console.error(`Invalid scene at index ${index}`, scene);
                  return null;
                }
                return (
                    <Scene
                        key={scene.id} // Use `id` as the key
                        sceneId={scene.id} // Pass `id` as the `sceneId` prop
                        highlightAll={highlightAll}
                        state={state}
                        highlightedPatterns={highlightedPatterns}
                        handleImageLoad={handleImageLoad}
                        calculateScaledPosition={calculateScaledPosition}
                    />
                );
              })}
            </div>
          </div>
        </div>
        );
        };

        export default StateDetails;
