// StateImage.jsx
import React from 'react';
import Image from "../image/image.component";
import './state-image.styles.css'

const StateImage = ({
                        stateImage,
                        transitions,
                        allStates,
                        onHover = () => {}, // Provide a default no-op function
                        onMouseLeave = () => {}, // Provide a default no-op function
                        onClick = () => {}, // Provide a default no-op function
                        isSelected = false
                    }) => {
    // Add a check to ensure stateImage and its properties exist
    if (!stateImage || !stateImage.patterns || stateImage.patterns.length === 0) {
        return <div>No image data available</div>;
    }

    const relevantTransitions = (() => {
        if (!transitions || !stateImage || !stateImage.involvedTransitionIds) {
            console.error('Missing required data for transitions', {
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
        ? `Transitions to state${relevantTransitions.length > 1 ? 's' : ''}: 
        ${relevantTransitions.map(t => getStateName(t.targetStateId)).join(', ')}`
        : 'No transitions';

    return (
        <div
            className={`state-image-container ${isSelected ? 'selected' : ''}`}
            onMouseEnter={() => onHover(stateImage)}
            onMouseLeave={onMouseLeave}
            onClick={() => onClick(stateImage)}
        >
            <Image
                image={stateImage.patterns[0].image}
                onLoad={() => console.log('Image loaded:', stateImage.id)}
                className="state-image"
            />
            <div className="state-image-name">{stateImage.name}</div>
            <div className="state-image-transitions">{transitionText}</div>
        </div>
    );
};

export default StateImage;