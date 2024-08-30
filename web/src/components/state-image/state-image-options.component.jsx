import React, { useState } from 'react';
import { ActionOptions } from '../../constants/action-options';

const StateImageOptions = ({
                               stateImage,
                               transitions,
                               allStates,
                               onCreateTransition,
                               onNameChange,
                               onDelete,
                               onMove,
                               onStateImageUpdate
                           }) => {
    const [targetState, setTargetState] = useState('');
    const [moveToState, setMoveToState] = useState('');
    const [newName, setNewName] = useState(stateImage.name);

    const handleNameChange = async () => {
        try {
            const response = await fetch(
                `http://localhost:8080/api/stateimages/${stateImage.id}/edit`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ name: newName }),
            });

            if (response.ok) {
                const updatedStateImage = await response.json();
                onStateImageUpdate(updatedStateImage);
                onNameChange(stateImage.id, newName);
                console.log('name changed');
            } else {
                console.error('Failed to update state image name');
            }
        } catch (error) {
            console.error('Error updating state image name:', error);
        }
    };

    const handleCreateTransition = async () => {
        if (!targetState) {
            console.error('No target state selected');
            return;
        }

        const transitionRequest = {
            sourceStateId: stateImage.ownerStateId,
            targetStateId: parseInt(targetState),
            stateImageId: stateImage.id,
            staysVisibleAfterTransition: 'FALSE',
            activate: [],
            exit: [],
            score: 0,
            timesSuccessful: 0,
            actionDefinition: {
                steps: [{
                    actionOptions: {
                        action: 'CLICK',
                        find: 'FIRST',
                        keepLargerMatches: false,
                        similarity: 0.9
                    },
                    objectCollection: {
                        stateImages: [{ id: stateImage.id }]
                    }
                }]
            }
        };

        console.log('Sending transition request:', transitionRequest);

        try {
            const response = await fetch('http://localhost:8080/api/transitions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(transitionRequest),
            });

            if (response.ok) {
                const data = await response.json();
                console.log('Received response:', data);

                // Handle new transition and updated StateImage
                if (data.transition && data.updatedStateImage) {
                    onCreateTransition(data.transition);
                    onStateImageUpdate(data.updatedStateImage);
                }

                setTargetState('');
            } else {
                const errorData = await response.text();
                console.error('Failed to create transition. Status:', response.status, 'Error:', errorData);
            }
        } catch (error) {
            console.error('Error creating transition:', error);
        }
    };

    const handleDelete = () => {
        if (window.confirm(`Are you sure you want to delete the state image "${stateImage.name}"?`)) {
            onDelete(stateImage.id);
        }
    };

    const handleMove = async () => {
        if (moveToState) {
            try {
                const response = await fetch(`http://localhost:8080/api/stateimages/${stateImage.id}/move`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ newStateId: parseInt(moveToState) }),
                });

                if (response.ok) {
                    const updatedStateImage = await response.json();
                    onStateImageUpdate(updatedStateImage);
                    onMove(stateImage.id, parseInt(moveToState));
                    setMoveToState('');
                } else {
                    console.error('Failed to move state image');
                }
            } catch (error) {
                console.error('Error moving state image:', error);
            }
        }
    };

    return (
        <div className="state-image-options">
            <div className="option-group">
                <select value={targetState} onChange={(e) => setTargetState(e.target.value)}>
                    <option value="">Click transition to state</option>
                    {allStates.map((state) => (
                        <option key={state.id} value={state.id}>
                            {state.id} {state.name}
                        </option>
                    ))}
                </select>
                <button onClick={handleCreateTransition}>Create</button>
            </div>

            <div className="option-group">
                <input
                    id="stateImageName"
                    placeholder="StateImage name"
                    type="text"
                    value={newName}
                    onChange={(e) => setNewName(e.target.value)}
                />
                <button onClick={handleNameChange}>Update</button>
            </div>

            <div className="option-group">
                <select value={moveToState} onChange={(e) => setMoveToState(e.target.value)}>
                    <option value="">Select state</option>
                    {allStates.map((state) => (
                        <option key={state.id} value={state.id}>
                            {state.name}
                        </option>
                    ))}
                </select>
                <button onClick={handleMove}>Move State Image</button>
            </div>

            <div className="option-group">
                <button onClick={handleDelete}>Delete State Image</button>
            </div>
        </div>
    );
};

export default StateImageOptions;