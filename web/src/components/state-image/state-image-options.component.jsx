import React, { useState, useCallback, useEffect } from 'react';
import axios from 'axios';
import { ActionOptions } from '../../constants/action-options';
import { Box, Typography, TextField, Button, Select, MenuItem, FormControl, InputLabel, List, ListItem,
    ListItemText, ListItemSecondaryAction, IconButton } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';

const StateImageOptions = ({
    stateImage,
    transitions,
    allStates,
    onCreateTransition,
    onNameChange,
    onDelete,
    onMove,
    onStateImageUpdate,
    currentStateId,
    selectedTransition,
    onTransitionUpdate,
    onTransitionDelete
}) => {
    const [transitionOptions, setTransitionOptions] = useState({
        staysVisibleAfterTransition: 'NONE',
        statesToEnter: [],
        statesToExit: [],
        score: 0
    });
    const [moveToState, setMoveToState] = useState('');
    const [newName, setNewName] = useState(stateImage.name);
    const [stateImageTransitions, setStateImageTransitions] = useState([]);

    useEffect(() => {
        // Filter transitions for the current state image
        const relevantTransitions = transitions.filter(t => t.stateImageId === stateImage.id);
        setStateImageTransitions(relevantTransitions);
    }, [transitions, stateImage.id]);

    const handleMove = useCallback(async () => {
        if (moveToState) {
            try {
                const response = await axios.put(`${process.env.REACT_APP_BROBOT_API_URL}/api/states/move-image`, {
                    stateImageId: stateImage.id,
                    newStateId: parseInt(moveToState)
                });

                if (response.status === 200) {
                    console.log('Move successful:', response.data);
                    onStateImageUpdate(stateImage.id, parseInt(moveToState));
                    setMoveToState('');
                } else {
                    console.error('Failed to move state image');
                }
            } catch (error) {
                console.error('Error moving state image:', error);
            }
        }
    }, [stateImage.id, moveToState, onStateImageUpdate]);

    const handleNameChange = async () => {
        try {
            const response = await fetch(
                `${process.env.REACT_APP_BROBOT_API_URL}/api/stateimages/${stateImage.id}/edit`, {
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
        if (transitionOptions.statesToEnter.length === 0) {
            console.error('No states to enter selected');
            return;
        }

        const transitionRequest = {
            sourceStateId: stateImage.ownerStateId,
            stateImageId: stateImage.id,
            staysVisibleAfterTransition: transitionOptions.staysVisibleAfterTransition,
            statesToEnter: transitionOptions.statesToEnter,
            statesToExit: transitionOptions.statesToExit,
            score: transitionOptions.score,
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

        try {
            const response = await axios.post('${process.env.REACT_APP_BROBOT_API_URL}/api/transitions', transitionRequest);
            if (response.status === 200 || response.status === 201) {
                const data = response.data;
                onCreateTransition(data.transition);
                onStateImageUpdate(data.updatedStateImage);
                // Reset transition options
                setTransitionOptions({
                    staysVisibleAfterTransition: 'NONE',
                    statesToEnter: [],
                    statesToExit: [],
                    score: 0
                });
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

    const handleTransitionOptionChange = (option, value) => {
        setTransitionOptions(prev => ({ ...prev, [option]: value }));
    };

    const handleTransitionUpdate = () => {
        if (selectedTransition) {
            const updatedTransition = {
                ...selectedTransition,
                ...transitionOptions
            };
            onTransitionUpdate(updatedTransition);
        }
    };

    const handleTransitionDelete = async (transitionId) => {
        try {
            const response = await axios.delete(`${process.env.REACT_APP_BROBOT_API_URL}/api/transitions/${transitionId}`);
            if (response.status === 204) {
                // Remove the deleted transition from the state
                setStateImageTransitions(prev => prev.filter(t => t.id !== transitionId));
                // Notify parent component
                onTransitionDelete(transitionId);
            }
        } catch (error) {
            console.error('Error deleting transition:', error);
        }
    };

    const getStateName = (stateId) => {
        const state = allStates.find(s => s.id === stateId);
        return state ? state.name : 'Unknown State';
    };

    return (
        <Box>
            <Typography variant="h6">State Image Options</Typography>
            <TextField
                fullWidth
                label="State Image Name"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                margin="normal"
            />
            <Button onClick={() => onNameChange(stateImage.id, newName)}>Update Name</Button>
            <Button onClick={() => onDelete(stateImage.id)}>Delete State Image</Button>

            <Typography variant="h6" sx={{ mt: 2 }}>Existing Transitions</Typography>
            <List>
                {stateImageTransitions.map((transition) => (
                    <ListItem key={transition.id}>
                        <ListItemText
                            primary={`Transition ${transition.id}`}
                            secondary={
                                <>
                                    <Typography component="span" variant="body2" color="text.primary">
                                        States to Enter: {transition.statesToEnter.map(getStateName).join(', ')}
                                    </Typography>
                                    <br />
                                    <Typography component="span" variant="body2" color="text.primary">
                                        States to Exit: {transition.statesToExit.map(getStateName).join(', ')}
                                    </Typography>
                                    <br />
                                    Stays Visible: {transition.staysVisibleAfterTransition}
                                    <br />
                                    Score: {transition.score}
                                </>
                            }
                        />
                        <ListItemSecondaryAction>
                            <Button onClick={() => handleTransitionDelete(transition.id)} >
                                Delete
                            </Button>
                        </ListItemSecondaryAction>
                    </ListItem>
                ))}
            </List>

            <Typography variant="h6" sx={{ mt: 2 }}>Create Transition</Typography>
            <FormControl fullWidth margin="normal">
                <InputLabel>Stays Visible After Transition</InputLabel>
                <Select
                    value={transitionOptions.staysVisibleAfterTransition}
                    onChange={(e) => handleTransitionOptionChange('staysVisibleAfterTransition', e.target.value)}
                >
                    <MenuItem value="NONE">Default</MenuItem>
                    <MenuItem value="TRUE">Yes</MenuItem>
                    <MenuItem value="FALSE">No</MenuItem>
                </Select>
            </FormControl>
            <FormControl fullWidth margin="normal">
                <InputLabel>States to Enter</InputLabel>
                <Select
                    multiple
                    value={transitionOptions.statesToEnter}
                    onChange={(e) => handleTransitionOptionChange('statesToEnter', e.target.value)}
                >
                    {allStates.map((state) => (
                        <MenuItem key={state.id} value={state.id}>
                            {state.name}
                        </MenuItem>
                    ))}
                </Select>
            </FormControl>
            <FormControl fullWidth margin="normal">
                <InputLabel>States to Exit</InputLabel>
                <Select
                    multiple
                    value={transitionOptions.statesToExit}
                    onChange={(e) => handleTransitionOptionChange('statesToExit', e.target.value)}
                >
                    {allStates.map((state) => (
                        <MenuItem key={state.id} value={state.id}>
                            {state.name}
                        </MenuItem>
                    ))}
                </Select>
            </FormControl>
            <TextField
                fullWidth
                type="number"
                label="Transition Score"
                value={transitionOptions.score}
                onChange={(e) => handleTransitionOptionChange('score', parseInt(e.target.value))}
                margin="normal"
            />
            <Button onClick={handleCreateTransition}>Create Transition</Button>

            {selectedTransition && (
                <Box sx={{ mt: 2 }}>
                    <Typography variant="h6">Modify Transition</Typography>
                    {/* Add fields to modify the selected transition */}
                    <Button onClick={handleTransitionUpdate}>Update Transition</Button>
                    <Button onClick={handleTransitionDelete}>Delete Transition</Button>
                </Box>
            )}

            <Typography variant="h6" sx={{ mt: 2 }}>Move State Image</Typography>
            <FormControl fullWidth margin="normal">
                <InputLabel>Move to State</InputLabel>
                <Select
                    value={moveToState}
                    onChange={(e) => setMoveToState(e.target.value)}
                >
                    {allStates
                        .filter(state => state.id !== currentStateId)
                        .map((state) => (
                            <MenuItem key={state.id} value={state.id}>
                                {state.name}
                            </MenuItem>
                        ))}
                </Select>
            </FormControl>
            <Button onClick={() => onMove(stateImage.id, moveToState)}>Move</Button>
        </Box>
    );
};

export default StateImageOptions;