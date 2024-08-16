import React, { useState, useEffect } from 'react';
import axios from 'axios';
import ImageSelector from './../../components/image/ImageSelector';
import NewStateDisplay from './NewStateDisplay';
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
      setNewState(response.data);
    } catch (error) {
      console.error('Error saving new state:', error);
      if (error.response) {
        console.error('Server error response:', error.response.data);
      }
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
      <ImageSelector
        images={images}
        selectedImages={selectedImages}
        handleImageSelect={handleImageSelect}
      />
      <button onClick={handleCreateState}>Create State</button>
      {newState && <NewStateDisplay newState={newState} />}
    </div>
  );
};

export default CreateState;