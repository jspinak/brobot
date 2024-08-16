import React from 'react';
import StateImage from './../../components/state-image/state-image.component';

const NewStateDisplay = ({ newState }) => {
  return (
    <div className="new-state-container">
      <h2>New State Created:</h2>
      <p>Name: {newState.name}</p>
      <div className="state-image-grid">
        {Array.isArray(newState.stateImages) && newState.stateImages.map((stateImage, index) => (
          <StateImage key={index} stateImage={stateImage} />
        ))}
      </div>
    </div>
  );
};

export default NewStateDisplay;
