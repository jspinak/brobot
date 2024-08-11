import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Image from './../../components/image/image.component'; // Adjust the import path as necessary
import StateImage from './../../components/state-image/state-image.component';
import './create-state.styles.css'

const CreateState = () => {
  const [stateName, setStateName] = useState('');
  const [images, setImages] = useState([]);
  const [selectedImages, setSelectedImages] = useState([]);
  const [newState, setNewState] = useState(null);

  useEffect(() => {
    fetchImages();
  }, []);

  const fetchImages = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/images/all');
      setImages(response.data);
    } catch (error) {
      console.error('Error fetching images:', error);
    }
  };

  const handleImageSelect = (imageId) => {
    setSelectedImages(prev =>
      prev.includes(imageId)
        ? prev.filter(id => id !== imageId)
        : [...prev, imageId]
    );
  };

    const handleCreateState = async () => {
        const newStateObj = {
          name: stateName,
          stateImages: selectedImages.map(imageId => {
            const image = images.find(img => img.id === imageId);
            if (!image) {
              console.error(`Image with id ${imageId} not found`);
              return null;
            }
            return {
              patterns: [{
                image: {
                  name: image.name,
                  imageBase64: image.imageBase64
                }
              }]
            };
          }).filter(Boolean) // Remove any null entries
        };

        if (newStateObj.stateImages.length === 0) {
          console.error('No valid images selected');
          // Maybe show an error message to the user
          return;
        }

        try {
          const response = await axios.post('http://localhost:8080/api/states', newStateObj, {
            headers: {
              'Content-Type': 'application/json',
            },
          });
          console.log('Server response:', JSON.stringify(response.data, null, 2));
          setNewState(response.data); // Assuming the server returns the created State object
        } catch (error) {
          console.error('Error saving new state:', error);
          if (error.response) {
            console.error('Server error response:', error.response.data);
          }
          // Maybe show an error message to the user
        }
    };

  return (
    <div>
      <h1>Create New State</h1>
      <input
        type="text"
        value={stateName}
        onChange={(e) => setStateName(e.target.value)}
        placeholder="Enter state name"
      />
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
      <button onClick={handleCreateState}>Create State</button>

        {newState && (
          <div className="new-state-container">  {/* New container class */}
            <h2>New State Created:</h2>
            <p>Name: {newState.name}</p>
            <div className="state-image-grid">
              {Array.isArray(newState.stateImages) && newState.stateImages.map((stateImage, index) => (
                <StateImage key={index} stateImage={stateImage} />
              ))}
            </div>
          </div>
        )}
    </div>
  );
};

export default CreateState;