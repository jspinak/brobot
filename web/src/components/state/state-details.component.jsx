import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import Image from './../image/image.component';
import StateImageSet from './../state-image-set/state-image-set.component';
import StateImageDetails from './../state-image/state-image-details.component';
import Highlight from './../highlight/highlight.component';
import './state-details.styles.css';
import HighlightAllCheckbox from './highlight-all-checkbox.component';
import StateDropdown from './../state-dropdown/state-dropdown.component';

const StateDetails = ({ allStates, isLoading }) => {
  const [state, setState] = useState(null);
  const [selectedStateImage, setSelectedStateImage] = useState(null);
  const [highlightedPatterns, setHighlightedPatterns] = useState([]);
  const [imageDimensions, setImageDimensions] = useState({ width: 0, height: 0 });
  const [highlightAll, setHighlightAll] = useState(false); // State for checkbox
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

  const calculateScaledPosition = (pattern) => {
    const scaleW = imageDimensions.width / 1920;
    const scaleH = imageDimensions.height / 1080;
    const adjustX = 10;
    const adjustY = 10;
    return { adjustX, adjustY, scaleW, scaleH };
  };

return (
    <div className="state-details">
      <StateDropdown states={allStates} currentStateId={stateId} />
      <HighlightAllCheckbox
        isChecked={highlightAll}
        onChange={handleCheckboxChange}
      />
      <div className="state-content">
        <div className="state-image-list">
          <StateImageSet
            stateImages={state.stateImages}
            onHover={handleStateImageHover}
            onMouseLeave={handleStateImageLeave}
            onClick={handleStateImageClick}
          />
        </div>
        <div className="state-scene">
          {state.scenes[0] && (
            <Image
              image={state.scenes[0]}
              onLoad={handleImageLoad}
            />
          )}
          {(highlightAll ? state.stateImages.flatMap(si => si.patterns) : highlightedPatterns).map((pattern) => {
            const { adjustX, adjustY, scaleW, scaleH } = calculateScaledPosition(pattern);
            return (
                <Highlight
                    key={pattern.id}
                    image={pattern.searchRegions.fixedRegion} // Pass the pattern object to the Highlight component
                    adjustX={adjustX}
                    adjustY={adjustY}
                    scaleW={scaleW}
                    scaleH={scaleH}
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
      <h2>State {state.name}</h2>
    </div>
  );
};

export default StateDetails;