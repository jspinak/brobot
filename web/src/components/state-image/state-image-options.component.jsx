import React, { useState, useCallback, useEffect, useContext } from 'react';
import { Box, Typography, TextField, Button, Select, MenuItem, FormControl, InputLabel, List, ListItem, ListItemText, ListItemSecondaryAction, Divider } from '@mui/material';
import { ProjectContext } from './../ProjectContext';
import api from './../../services/api';

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
    onTransitionDelete,
    fetchTransitions
}) => {
    const { selectedProject } = useContext(ProjectContext);
    const [transitionOptions, setTransitionOptions] = useState({
        staysVisibleAfterTransition: 'NONE',
        statesToEnter: [],
        statesToExit: [],
        score: 0
    });
    const [moveToState, setMoveToState] = useState('');
    const [newName, setNewName] = useState('');
    const [stateImageTransitions, setStateImageTransitions] = useState([]);

    useEffect(() => {
        if (stateImage) {
            setNewName(stateImage.name);
            setStateImageTransitions(transitions.filter(t => t.stateImageId === stateImage.id));
        }
    }, [stateImage, transitions]);

    const handleMove = useCallback(async () => {
        if (moveToState && stateImage) {
            try {
                const response = await api.put(`/api/states/move-image`, {
                    stateImageId: stateImage.id,
                    newStateId: parseInt(moveToState)
                });

                if (response.status === 200) {
                    onStateImageUpdate(stateImage.id, parseInt(moveToState));
                    setMoveToState('');
                } else {
                    console.error('Failed to move state image');
                }
            } catch (error) {
                console.error('Error moving state image:', error);
            }
        }
    }, [stateImage, moveToState, onStateImageUpdate]);

    const handleNameChange = async () => {
        if (stateImage) {
            try {
                const response = await api.put(`/api/stateimages/${stateImage.id}/edit`, { name: newName });
                if (response.ok) {
                    const updatedStateImage = await response.json();
                    onStateImageUpdate(updatedStateImage);
                    onNameChange(stateImage.id, newName);
                } else {
                    console.error('Failed to update state image name');
                }
            } catch (error) {
                console.error('Error updating state image name:', error);
            }
        }
    };

    const handleCreateTransition = async () => {
        if (transitionOptions.statesToEnter.length === 0 || !selectedProject) {
            console.error('No states to enter selected or no project selected');
            return;
        }

        const transitionRequest = {
            projectId: selectedProject.id,
            sourceStateId: stateImage.ownerStateId,
            stateImageId: stateImage.id,
            ...transitionOptions,
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
            console.log('stateImage:', stateImage);
            console.log('ownerStateId:', stateImage.ownerStateId);
            console.log('currentStateId:', currentStateId);
            const response = await api.post('/api/transitions', transitionRequest);
            if (response.status === 200 || response.status === 201) {
                onCreateTransition(response.data.transition);
                onStateImageUpdate(response.data.updatedStateImage);
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

    const handleTransitionOptionChange = (option, value) => {
        setTransitionOptions(prev => ({ ...prev, [option]: value }));
    };

const handleTransitionDelete = async (transitionId) => {
    try {
        const response = await api.delete(`/api/transitions/${transitionId}`);
        if (response.status === 204) {
            setStateImageTransitions(prev => prev.filter(t => t.id !== transitionId));
            onTransitionDelete(transitionId);
        }
    } catch (error) {
        console.error('Error deleting transition:', error);
        if (error.response) {
            console.error('Error response:', error.response.data);
            // You can show this error to the user
            // For example: setErrorMessage(error.response.data);
        }
        // Optionally, refresh the transitions list to ensure it's up-to-date
        fetchTransitions();
    }
};

    const getStateName = (stateId) => {
        const state = allStates.find(s => s.id === stateId);
        return state ? state.name : 'Unknown State';
    };

    if (!stateImage) {
        return <Typography>No state image selected</Typography>;
    }

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
            <Button onClick={handleNameChange}>Update Name</Button>
            <Button onClick={() => onDelete(stateImage.id)}>Delete State Image</Button>

            <Divider sx={{ my: 2 }} />

            <Typography variant="h6">Existing Transitions</Typography>
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
                            <Button onClick={() => handleTransitionDelete(transition.id)}>Delete</Button>
                        </ListItemSecondaryAction>
                    </ListItem>
                ))}
            </List>

            <Divider sx={{ my: 2 }} />

            <Typography variant="h6">Create Transition</Typography>
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
                        <MenuItem key={state.id} value={state.id}>{state.name}</MenuItem>
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
                        <MenuItem key={state.id} value={state.id}>{state.name}</MenuItem>
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

            <Divider sx={{ my: 2 }} />

            <Typography variant="h6">Move State Image</Typography>
            <FormControl fullWidth margin="normal">
                <InputLabel>Move to State</InputLabel>
                <Select
                    value={moveToState}
                    onChange={(e) => setMoveToState(e.target.value)}
                >
                    {allStates
                        .filter(state => state.id !== currentStateId)
                        .map((state) => (
                            <MenuItem key={state.id} value={state.id}>{state.name}</MenuItem>
                        ))}
                </Select>
            </FormControl>
            <Button onClick={handleMove}>Move</Button>
        </Box>
    );
};

export default StateImageOptions;