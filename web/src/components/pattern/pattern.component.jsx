import React, { useState, useEffect } from 'react';
import { Box, Typography, CircularProgress } from '@mui/material';
import Image from '../image/image.component';
import api from '../../services/api';

const Pattern = ({ pattern, isSelected, onSelect }) => {
  const [imageData, setImageData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchImage = async () => {
      if (!pattern || !pattern.imageId) {
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const response = await api.get(`/api/images/${pattern.imageId}`);
        setImageData(response.data);
      } catch (error) {
        console.error('Error fetching image:', error);
        setError('Failed to load image');
      } finally {
        setLoading(false);
      }
    };

    fetchImage();
  }, [pattern]);

  if (!pattern) {
    return null;
  }

  return (
    <Box
      onClick={() => onSelect(pattern.id)}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: 'lightblue',
        border: 3,
        borderColor: isSelected ? 'green' : 'transparent',
        borderRadius: 1,
        p: 0.25,
        cursor: 'pointer',
        transition: 'transform 0.25s ease-out, border-color 0.25s ease-out, box-shadow 0.25s ease-out',
        '&:hover': {
          transform: 'scale(1.05)',
        },
        ...(isSelected && {
          boxShadow: '0 0 10px rgba(0, 255, 0, 0.5)',
          '&:hover': {
            transform: 'scale(1.05)',
            boxShadow: '0 0 15px rgba(0, 255, 0, 0.7)',
          },
        }),
      }}
    >
      {loading ? (
        <CircularProgress />
      ) : error ? (
        <Typography color="error">{error}</Typography>
      ) : (
        imageData && <Image image={imageData} />
      )}
      <Typography variant="caption" sx={{ mt: -1, fontSize: '10px' }}>
        {pattern.id} {pattern.name}
      </Typography>
    </Box>
  );
};

export default Pattern;