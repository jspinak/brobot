import React, { useState } from 'react';
import Image from '../image/image.component';
import './state-image-details.styles.css';

const StateImageDetails = ({ stateImage, allStates }) => {
    const [selectedTargetState, setSelectedTargetState] = useState('');

    const handleTargetStateChange = (e) => {
        setSelectedTargetState(e.target.value);
    };

    const handleCreateTransition = () => {
        // Implement logic to create a transition
        console.log(`Create transition from ${stateImage.ownerStateName} to ${selectedTargetState}`);
    };

    return (
        <div className="state-image-details">
            <h3>{stateImage.name}</h3>
            {stateImage.patterns.map((pattern) => (
                <Image key={pattern.id} image={pattern.image} />
            ))}
            <div className="state-image-options">
                <select value={selectedTargetState} onChange={handleTargetStateChange}>
                    <option value="">Select target state</option>
                    {allStates.map((state) => (
                        <option key={state.id} value={state.name}>
                            {state.name}
                        </option>
                    ))}
                </select>
                <button onClick={handleCreateTransition}>Create Transition</button>
            </div>
        </div>
    );
};

export default StateImageDetails;
