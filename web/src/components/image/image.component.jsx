import React from 'react';
import { Box, Typography } from '@mui/material';

const Image = ({ image, onLoad }) => {
    if (!image) {
        return <Box sx={{ color: 'error.main' }}>Image data missing</Box>;
    }

    const { id, name, imageBase64 } = image;

    // Check if imageBase64 is present
    if (!imageBase64) {
        return <Box sx={{ color: 'error.main' }}>Image data incomplete</Box>;
    }

    // Ensure the base64 string is properly formatted
    const imageSrc = `data:image/png;base64,${imageBase64.trim()}`;

    return (
        <Box key={id} sx={{ backgroundColor: 'background.paper', border: 1, borderColor: 'grey.300', borderRadius: 1, p: 1 }}>
            <img
                src={imageSrc}
                alt={name}
                onLoad={onLoad}
                onError={(e) => {
                    console.error(`Error loading image ${id} (${name}):`, e);
                    e.target.onerror = null; // Prevents infinite loop if fallback image also fails
                    e.target.src = '/path/to/fallback/image.png'; // Ensure this path is correct
                }}
                style={{ maxWidth: '100%', height: 'auto' }}
            />
        </Box>
    );
};

export default Image;
