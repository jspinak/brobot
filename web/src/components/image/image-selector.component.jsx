import React from 'react';
import Image from './image.component';

const ImageSelector = ({ images, selectedImages, handleImageSelect }) => {
  return (
    <div className="image-grid">
      {images.map(image => (
        <div key={image.id} className="image-container">
          <input
            type="checkbox"
            id={`image-${image.id}`}
            checked={selectedImages.includes(image.id)}
            onChange={() => handleImageSelect(image.id)}
          />
          <label htmlFor={`image-${image.id}`}>
            <Image image={image} />
          </label>
        </div>
      ))}
    </div>
  );
};

export default ImageSelector;