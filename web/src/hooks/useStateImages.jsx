import { useState, useCallback } from 'react';
import api from '../services/api';

const useStateImages = (state, setAllStates, stateId) => {
  const [selectedStateImage, setSelectedStateImage] = useState(null);

  const handleStateImageClick = useCallback((stateImage) => {
    setSelectedStateImage(stateImage);
  }, []);

  const handleStateImageUpdate = useCallback((stateImageId, newStateId) => {
    setAllStates(prevAllStates => {
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

      return prevAllStates.map(stateItem => {
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
    });
  }, [stateId, setAllStates]);

  const handleStateImageNameChange = useCallback((stateImageId, newName) => {
    setAllStates(prevAllStates =>
      prevAllStates.map(stateItem => ({
        ...stateItem,
        stateImages: stateItem.stateImages.map(stateImage =>
          stateImage.id === stateImageId
            ? { ...stateImage, name: newName }
            : stateImage
        )
      }))
    );
  }, [setAllStates]);

  const handleDeleteStateImage = useCallback(async (stateImageId) => {
    try {
      const response = await api.delete(`/api/stateimages/${stateImageId}`);
      if (response.status === 200) {
        setAllStates(prevAllStates =>
          prevAllStates.map(stateItem => ({
            ...stateItem,
            stateImages: stateItem.stateImages.filter(si => si.id !== stateImageId)
          }))
        );
        setSelectedStateImage(null);
      } else {
        console.error('Failed to delete state image');
      }
    } catch (error) {
      console.error('Error deleting state image:', error);
    }
  }, [setAllStates]);

  const handleMoveStateImage = useCallback(async (stateImageId, newStateId) => {
    try {
      const response = await api.put(`/api/states/move-image`, {
        stateImageId,
        newStateId
      });

      if (response.status === 200) {
        handleStateImageUpdate(stateImageId, newStateId);
      } else {
        console.error('Failed to move state image, status:', response.status);
      }
    } catch (error) {
      console.error('Error moving state image:', error);
    }
  }, [handleStateImageUpdate]);

  return {
    stateImages: state?.stateImages || [],
    selectedStateImage,
    handleStateImageClick,
    handleStateImageUpdate,
    handleStateImageNameChange,
    handleDeleteStateImage,
    handleMoveStateImage
  };
};

export default useStateImages;