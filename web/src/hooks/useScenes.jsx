// hooks/useScenes.jsx
import { useState, useCallback } from 'react';

const useScenes = (state) => {
  const [highlightedPatterns, setHighlightedPatterns] = useState([]);
  const [imageDimensions, setImageDimensions] = useState({});

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

  const handleSceneImageLoad = useCallback((event, sceneId) => {
    setImageDimensions(prevDimensions => ({
      ...prevDimensions,
      [sceneId]: {
        width: event.target.width,
        height: event.target.height
      }
    }));
  }, []);

  const calculateScaledPosition = useCallback((pattern, sceneId) => {
    const dimensions = imageDimensions[sceneId] || { width: 1920, height: 1080 };
    const scaleW = dimensions.width / 1920;
    const scaleH = dimensions.height / 1080;
    const adjustX = 10;
    const adjustY = 10;
    return { adjustX, adjustY, scaleW, scaleH };
  }, [imageDimensions]);

  return {
    scenes: state?.scenes || [],
    highlightedPatterns,
    handleStateImageHover,
    handleStateImageLeave,
    handleSceneImageLoad,
    calculateScaledPosition
  };
};

export default useScenes;