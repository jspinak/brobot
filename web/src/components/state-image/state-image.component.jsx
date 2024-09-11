import React from 'react';
import { Box, Typography } from '@mui/material';
import Image from "../image/image.component";

const StateImage = ({
    stateImage,
    transitions = [],
    allStates = [],
    isSelected = false
}) => {
    if (!stateImage || !stateImage.patterns || stateImage.patterns.length === 0) {
        return <Typography>No image data available</Typography>;
    }

    const relevantTransitions = (() => {
        if (!Array.isArray(transitions) || !stateImage || !Array.isArray(stateImage.involvedTransitionIds)) {
            console.warn('Missing or invalid data for transitions', {
                transitions,
                stateImage,
                involvedTransitionIds: stateImage?.involvedTransitionIds
            });
            return [];
        }
        return transitions
            .filter(t => stateImage.involvedTransitionIds.includes(t.id))
            .filter(t => t.sourceStateId === stateImage.ownerStateId);
    })();

    const getStateName = (stateId) => {
        const state = allStates.find(s => s.id === stateId);
        return state ? state.name : `Unknown State (ID: ${stateId})`;
    };

    const transitionText = relevantTransitions.length > 0
        ? `Transitions to states:
        ${relevantTransitions.map(t =>
            `${t.statesToEnter.map(getStateName).join(', ')}`
        ).join(' ')}`
        : 'No transitions';

    return (
        <Box
            sx={{
                backgroundColor: isSelected ? '#c0c0c0' : '#e0e0e0',
                p: 1,
                borderRadius: 1,
                width: '100%'
            }}
        >
            <Image
                image={stateImage.patterns[0].image}
                onLoad={() => console.log('Image loaded:', stateImage.id)}
                sx={{ maxWidth: 'calc(100% - 40px)', maxHeight: 100, objectFit: 'contain', display: 'block', mx: 'auto' }}
            />
            <Typography variant="subtitle2" align="center" sx={{ mt: 1 }}>{stateImage.name}</Typography>
            <Typography variant="caption" align="center" sx={{ display: 'block', mt: 0.5 }}>{transitionText}</Typography>
        </Box>
    );
};

export default StateImage;