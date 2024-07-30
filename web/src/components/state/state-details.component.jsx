// StateDetails.js
import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import Image from './../image/image.component';
import StateImageSet from './../state-image-set/state-image-set.component';
import StateImageDetails from './../state-image/state-image-details.component';
import './state-details.styles.css'; // We'll create this CSS file

const StateDetails = ({ allStates, isLoading }) => {
  const [state, setState] = useState(null);
  const [selectedStateImage, setSelectedStateImage] = useState(null);
  const [highlightedPatterns, setHighlightedPatterns] = useState([]);
  const [imageDimensions, setImageDimensions] = useState({ width: 0, height: 0 });
  const { stateId } = useParams();

    useEffect(() => {
      console.log('highlightedPatterns updated:', highlightedPatterns);
    }, [highlightedPatterns]);

  useEffect(() => {
    if (!isLoading && allStates.length > 0) {
      const foundState = allStates.find(s => s.id === parseInt(stateId));
      setState(foundState || null);
    }

      return () => {
        console.log('StateDetails component is unmounting');
      };
    }, [stateId, allStates, isLoading]);

  const handleImageLoad = (event) => {
    setImageDimensions({
      width: event.target.width,
      height: event.target.height
    });
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

  const handleStateImageClick = (stateImage) => {
    setSelectedStateImage(stateImage);
  };

  if (isLoading || !state) {
    return <div>Loading...</div>;
  }

  return (
    <div className="state-details">
      <h2>{state.name}</h2>
      <div className="state-content">
        <div className="state-image-list">
          <StateImageSet
            stateImages={state.stateImages}
            onHover={handleStateImageHover}
            onClick={handleStateImageClick}
          />
        </div>
        <div className="state-illustration">
          {state.scenes[0] && (
            <Image
              image={state.scenes[0]}
              onLoad={handleImageLoad}
            />
          )}
          {highlightedPatterns.map((pattern) => {
            const scaleX = imageDimensions.width / 1900; // Assuming 1900 is the original width
            const scaleY = imageDimensions.height / (1900 * 9 / 16); // Assuming 16:9 aspect ratio

            const scaledX = pattern.searchRegions.fixedRegion.x * scaleX;
            const scaledY = pattern.searchRegions.fixedRegion.y * scaleY;
            const scaledWidth = pattern.searchRegions.fixedRegion.w * scaleX;
            const scaledHeight = pattern.searchRegions.fixedRegion.h * scaleY;

          return (
            <div
              key={pattern.id}
              className="highlight"
              style={{
                position: 'absolute',
                left: `${scaledX}px`,
                top: `${scaledY}px`,
                width: `${scaledWidth}px`,
                height: `${scaledHeight}px`,
                border: '2px solid red',
                pointerEvents: 'none',
              }}
            />
          );
        })}
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