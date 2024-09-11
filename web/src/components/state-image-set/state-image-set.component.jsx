import React from 'react';
import { Box, Paper, Typography } from '@mui/material';
import StateImage from './../state-image/state-image.component';

const StateImageSet = ({
    stateImages,
    transitions,
    onHover,
    onMouseLeave,
    onClick,
    selectedStateImage,
    allStates,
    currentStateId
}) => {
    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {stateImages.map((stateImage) => (
                <Paper key={stateImage.id} elevation={3} sx={{ p: 2 }}>
                    <Box
                        sx={{
                            backgroundColor: '#e0e0e0',
                            p: 1,
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            cursor: 'pointer'
                        }}
                        onMouseEnter={() => onHover(stateImage)}
                        onMouseLeave={onMouseLeave}
                        onClick={() => onClick(stateImage)}
                    >
                        <StateImage
                            stateImage={stateImage}
                            transitions={transitions}
                            allStates={allStates}
                            isSelected={selectedStateImage && selectedStateImage.id === stateImage.id}
                        />
                    </Box>
                </Paper>
            ))}
        </Box>
    );
};

export default StateImageSet;