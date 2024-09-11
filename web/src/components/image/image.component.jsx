import React from 'react';
import { Box, Typography } from '@mui/material';

const Image = ({ image, onLoad }) => {
    if (!image) {
        console.error('No image data provided');
        return <Box sx={{ color: 'error.main' }}>Image data missing</Box>;
    }

    const { id, name, imageBase64 } = image;

    if (!imageBase64) {
        console.error(`Image ${id} (${name}) has no base64 data`);
        return <Box sx={{ color: 'error.main' }}>Image data incomplete</Box>;
    }

    return (
        <Box key={id} sx={{ backgroundColor: 'background.paper', border: 1, borderColor: 'grey.300', borderRadius: 1, p: 1 }}>
            <img
                src={`data:image/png;base64, ${imageBase64}`}
                alt={name}
                onLoad={onLoad}
                onError={(e) => {
                    console.error(`Error loading image ${id} (${name}):`, e);
                    e.target.onerror = null;
                    e.target.src = 'path/to/fallback/image.png';
                }}
                style={{ maxWidth: '100%', height: 'auto' }}
            />
        </Box>
    );
};

export default Image;