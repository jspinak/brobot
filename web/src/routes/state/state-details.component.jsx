import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import Image from './../../components/image/image.component';
import StateImageSet from './../../components/state-image-set/state-image-set.component';
import StateImageDetails from './../../components/state-image/state-image-details.component';
import Highlight from './../../components/highlight/highlight.component';
import './state-details.styles.css';
import HighlightAllCheckbox from './highlight-all-checkbox.component';
import StateDropdown from './../../components/state-dropdown/state-dropdown.component';

const StateDetails = ({ allStates, isLoading }) => {
  const [state, setState] = useState(null);
  const [selectedStateImage, setSelectedStateImage] = useState(null);
  const [highlightedPatterns, setHighlightedPatterns] = useState([]);
  const [imageDimensions, setImageDimensions] = useState({});
  const [highlightAll, setHighlightAll] = useState(true);
  const [selectedImages, setSelectedImages] = useState([]);
  const [selectedStateImages, setSelectedStateImages] = useState([]);
  const [isEditing, setIsEditing] = useState(false);
  const { stateId } = useParams();

  useEffect(() => {
    if (!isLoading && allStates.length > 0) {
      const foundState = allStates.find(s => s.id === parseInt(stateId));
      setState(foundState || null);
    }
    return () => {
      console.log('StateDetails component is unmounting');
    };
  }, [stateId, allStates, isLoading]);

  const handleImageLoad = (event, sceneId) => {
    setImageDimensions(prevDimensions => ({
      ...prevDimensions,
      [sceneId]: {
        width: event.target.width,
        height: event.target.height
      }
    }));
  };

  const handleStateImageHover = useCallback((stateImage) => {
    console.log('handleStateImageHover called with:', stateImage);
    if (stateImage && Array.isArray(stateImage.patterns)) {
      setHighlightedPatterns(prevPatterns => {
        console.log('Updating patterns:', stateImage.patterns);
        return stateImage.patterns;
      });
    } else {
      console.error('Invalid stateImage or patterns:', stateImage);
    }
  }, []);

  const handleStateImageLeave = () => {
    setHighlightedPatterns([]);
  };

  const handleStateImageClick = (stateImage) => {
    setSelectedStateImage(stateImage);
  };

  const handleCheckboxChange = () => {
    setHighlightAll(!highlightAll);
  };

  if (isLoading || !state) {
    return <div>Loading...</div>;
  }

  const handleStateImageCheckboxChange = (stateImageId) => {
    setSelectedStateImages((prevSelected) =>
      prevSelected.includes(stateImageId)
        ? prevSelected.filter(id => id !== stateImageId)
        : [...prevSelected, stateImageId]
    );
  };

    const handleEditToggle = () => {
      setIsEditing(!isEditing);
    };

  const handleDeleteImages = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/state-images/delete', {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ stateImageIds: selectedStateImages }),
      });

      if (response.ok) {
        // Filter out deleted state images from the state
        const updatedStateImages = state.stateImages.filter((stateImage) =>
          !selectedStateImages.includes(stateImage.id)
        );
        setState({ ...state, stateImages: updatedStateImages });
        setSelectedStateImages([]);
      } else {
        console.error('Failed to delete state images');
      }
    } catch (error) {
      console.error('Error deleting state images:', error);
    }
  };

    const handleNameChange = async (stateImageId, newName) => {
        try {
          const response = await fetch(`http://localhost:8080/api/state-images/${stateImageId}/edit`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({ name: newName }),
          });

          if (response.ok) {
            // Update the state image name in the state
            const updatedStateImages = state.stateImages.map((stateImage) =>
              stateImage.id === stateImageId
                ? { ...stateImage, name: newName }
                : stateImage
            );
            setState({ ...state, stateImages: updatedStateImages });
          } else {
            console.error('Failed to edit state image name');
          }
        } catch (error) {
          console.error('Error editing state image name:', error);
        }
    };

  const calculateScaledPosition = (pattern, sceneId) => {
    const dimensions = imageDimensions[sceneId] || { width: 1920, height: 1080 };
    const scaleW = dimensions.width / 1920;
    const scaleH = dimensions.height / 1080;
    const adjustX = 10;
    const adjustY = 10;
    return { adjustX, adjustY, scaleW, scaleH };
  };

  return (
    <div className="state-details">
      <StateDropdown states={allStates} currentStateId={stateId} />
      <div className="action-buttons">
        <HighlightAllCheckbox
          isChecked={highlightAll}
          onChange={handleCheckboxChange}
        />
        <button onClick={handleEditToggle}>{isEditing ? 'Stop Editing' : 'Edit Selected'}</button>
        <button onClick={handleDeleteImages} disabled={selectedImages.length === 0}>Delete Selected</button>
      </div>
      <div className="state-content">
        <div className="state-image-list">
          <StateImageSet
            stateImages={state.stateImages}  // Ensure this is correctly passed
            onHover={handleStateImageHover}
            onMouseLeave={handleStateImageLeave}
            onClick={handleStateImageClick}
            onCheckboxChange={handleStateImageCheckboxChange}
            selectedImages={selectedStateImages}
            isEditing={isEditing}
            onNameChange={handleNameChange}
          />
        </div>
        <div className="state-scenes">
          {state.scenes.map((scene, index) => (
            <div key={scene.id} className="state-scene">
              <Image
                image={scene}
                onLoad={(event) => handleImageLoad(event, scene.id)}
              />
              {(highlightAll ? state.stateImages.flatMap(si => si.patterns) : highlightedPatterns).map((pattern) => {
                const { adjustX, adjustY, scaleW, scaleH } = calculateScaledPosition(pattern, scene.id);
                return (
                  <Highlight
                    key={`${pattern.id}-${scene.id}`}
                    image={pattern.searchRegions.fixedRegion}
                    adjustX={adjustX}
                    adjustY={adjustY}
                    scaleW={scaleW}
                    scaleH={scaleH}
                  />
                );
              })}
            </div>
          ))}
        </div>
      </div>
      {selectedStateImage && (
        <StateImageDetails
          stateImage={selectedStateImage}
          allStates={allStates}
        />
      )}
    </div>
  );
};

export default StateDetails;