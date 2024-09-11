import React from 'react';
import { Box, Typography } from '@mui/material';
import Image from '../image/image.component';

const Pattern = ({ pattern, isSelected, onSelect }) => {
  if (!pattern || !pattern.image) {
    console.error('Pattern or pattern image is missing');
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
      <Image image={pattern.image} />
      <Typography variant="caption" sx={{ mt: -1, fontSize: '10px' }}>
        {pattern.id} {pattern.name}
      </Typography>
    </Box>
  );
};

export default Pattern;