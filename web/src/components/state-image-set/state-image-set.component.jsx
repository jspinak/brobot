import React from 'react';
import './state-image-set.styles.css';

const StateImageSet = ({ stateImages, onHover, onMouseLeave, onClick, onCheckboxChange, selectedImages, isEditing, onNameChange }) => {
  if (!stateImages || stateImages.length === 0) {
    return <div>No images available</div>;
  }

  return (
    <div className="state-image-set">
      {stateImages.map((stateImage) => (
        <div
          key={stateImage.id}
          className="state-image-item"
          onMouseEnter={() => onHover(stateImage)}
          onMouseLeave={onMouseLeave}
        >
          <img
            src={stateImage.thumbnailUrl}
            alt={stateImage.name}
            onClick={() => onClick(stateImage)}
          />
          {isEditing && (
            <input
              type="text"
              value={stateImage.name}
              onChange={(e) => onNameChange(stateImage.id, e.target.value)}
              className="state-image-edit-input"
            />
          )}
          <input
            type="checkbox"
            checked={selectedImages.includes(stateImage.id)}
            onChange={() => onCheckboxChange(stateImage.id)}
            className="state-image-checkbox"
          />
        </div>
      ))}
    </div>
  );
};

export default StateImageSet;
