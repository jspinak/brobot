import React, { useState, useEffect, useContext } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  TextField,
  Button,
  IconButton,
  Select,
  MenuItem,
  FormControl,
  InputLabel
} from '@mui/material';
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import { Plus, Save, Edit, Trash2 } from 'lucide-react';
import { ProjectContext } from './../../components/ProjectContext';
import api from './../../services/api';

const ActionDefinitionBuilder = () => {
  const [actionDefinition, setActionDefinition] = useState({
    name: '',
    actionType: '',
    steps: []
  });
  const [selectedStateImages, setSelectedStateImages] = useState([]);
  const [states, setStates] = useState([]);
  const [currentStep, setCurrentStep] = useState(null);
  const { selectedProject } = useContext(ProjectContext);

  useEffect(() => {
    if (!selectedProject) return;

    const fetchStates = async () => {
      try {
        const response = await api.get(`/api/states/project/${selectedProject.id}`);
        setStates(response.data);
      } catch (error) {
        console.error('Error fetching states:', error);
      }
    };
    fetchStates();
  }, [selectedProject]);

  const handleDragEnd = (result) => {
    if (!result.destination) return;

    const items = Array.from(actionDefinition.steps);
    const [reorderedItem] = items.splice(result.source.index, 1);
    items.splice(result.destination.index, 0, reorderedItem);

    setActionDefinition({
      ...actionDefinition,
      steps: items
    });
  };

  const handleSave = async () => {
    try {
      const response = await api.post('/api/action-definitions', {
        name: actionDefinition.name,
        actionType: actionDefinition.actionType,
        steps: actionDefinition.steps.map(step => ({
          actionOptions: {
            action: step.action,
            find: step.find,
            similarity: step.similarity,
            maxWait: step.maxWait
          },
          objectCollection: {
            stateImages: step.selectedImages.map(id => ({ id }))
          }
        }))
      });
      console.log('Saved action definition:', response.data);
    } catch (error) {
      console.error('Error saving action definition:', error);
    }
  };

  const handleAddStep = () => {
    setCurrentStep({
      action: 'FIND',
      find: 'FIRST',
      similarity: 0.7,
      maxWait: 0,
      selectedImages: []
    });
  };

  const handleStepSave = () => {
    if (!currentStep) return;

    setActionDefinition(prev => ({
      ...prev,
      steps: [...prev.steps, currentStep]
    }));
    setCurrentStep(null);
  };

  const handleStateImageSelect = (imageId) => {
    if (!currentStep) return;

    setCurrentStep(prev => ({
      ...prev,
      selectedImages: prev.selectedImages.includes(imageId)
        ? prev.selectedImages.filter(id => id !== imageId)
        : [...prev.selectedImages, imageId]
    }));
  };

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h5" gutterBottom>Action Definition</Typography>
        <Grid container spacing={2}>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="Name"
              value={actionDefinition.name}
              onChange={(e) => setActionDefinition(prev => ({
                ...prev,
                name: e.target.value
              }))}
            />
          </Grid>
          <Grid item xs={6}>
            <FormControl fullWidth>
              <InputLabel>Action Type</InputLabel>
              <Select
                value={actionDefinition.actionType}
                onChange={(e) => setActionDefinition(prev => ({
                  ...prev,
                  actionType: e.target.value
                }))}
              >
                <MenuItem value="BASIC">Basic Action</MenuItem>
                <MenuItem value="COMPOSITE">Composite Action</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      <DragDropContext onDragEnd={handleDragEnd}>
        <Droppable droppableId="steps">
          {(provided) => (
            <div {...provided.droppableProps} ref={provided.innerRef}>
              {actionDefinition.steps.map((step, index) => (
                <Draggable
                  key={`step-${index}`}
                  draggableId={`step-${index}`}
                  index={index}
                >
                  {(provided) => (
                    <Paper
                      ref={provided.innerRef}
                      {...provided.draggableProps}
                      {...provided.dragHandleProps}
                      sx={{ p: 2, mb: 2 }}
                    >
                      <Grid container alignItems="center" spacing={2}>
                        <Grid item xs={3}>
                          <Typography>{step.action}</Typography>
                        </Grid>
                        <Grid item xs={3}>
                          <Typography>
                            Images: {step.selectedImages.length}
                          </Typography>
                        </Grid>
                        <Grid item xs={6} container justifyContent="flex-end">
                          <IconButton
                            onClick={() => setCurrentStep({...step, index})}
                          >
                            <Edit size={20} />
                          </IconButton>
                          <IconButton
                            onClick={() => setActionDefinition(prev => ({
                              ...prev,
                              steps: prev.steps.filter((_, i) => i !== index)
                            }))}
                          >
                            <Trash2 size={20} />
                          </IconButton>
                        </Grid>
                      </Grid>
                    </Paper>
                  )}
                </Draggable>
              ))}
              {provided.placeholder}
            </div>
          )}
        </Droppable>
      </DragDropContext>

      <Box sx={{ mt: 2, mb: 2 }}>
        <Button
          variant="contained"
          startIcon={<Plus />}
          onClick={handleAddStep}
          sx={{ mr: 2 }}
        >
          Add Step
        </Button>
        <Button
          variant="contained"
          color="primary"
          startIcon={<Save />}
          onClick={handleSave}
          disabled={actionDefinition.steps.length === 0}
        >
          Save Definition
        </Button>
      </Box>

      {currentStep && (
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>Configure Step</Typography>
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>Action</InputLabel>
                <Select
                  value={currentStep.action}
                  onChange={(e) => setCurrentStep(prev => ({
                    ...prev,
                    action: e.target.value
                  }))}
                >
                  <MenuItem value="FIND">Find</MenuItem>
                  <MenuItem value="CLICK">Click</MenuItem>
                  <MenuItem value="TYPE">Type</MenuItem>
                  <MenuItem value="MOVE">Move</MenuItem>
                  <MenuItem value="VANISH">Vanish</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                type="number"
                label="Similarity"
                value={currentStep.similarity}
                onChange={(e) => setCurrentStep(prev => ({
                  ...prev,
                  similarity: parseFloat(e.target.value)
                }))}
              />
            </Grid>
          </Grid>

          <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>Select State Images</Typography>
          <Grid container spacing={2}>
            {states.map(state => (
              <Grid item xs={12} key={state.id}>
                <Typography variant="subtitle1">{state.name}</Typography>
                <Grid container spacing={1}>
                  {state.stateImages.map(image => (
                    <Grid item key={image.id}>
                      <Paper
                        sx={{
                          p: 1,
                          cursor: 'pointer',
                          border: currentStep.selectedImages.includes(image.id) ? 2 : 0,
                          borderColor: 'primary.main'
                        }}
                        onClick={() => handleStateImageSelect(image.id)}
                      >
                        {image.imagesBase64?.map((base64, idx) => (
                          <img
                            key={idx}
                            src={`data:image/png;base64,${base64}`}
                            alt={`${image.name}-${idx}`}
                            style={{ maxWidth: 100, marginBottom: 4 }}
                          />
                        ))}
                        <Typography variant="caption" display="block">
                          {image.name}
                        </Typography>
                      </Paper>
                    </Grid>
                  ))}
                </Grid>
              </Grid>
            ))}
          </Grid>

          <Button
            variant="contained"
            onClick={handleStepSave}
            sx={{ mt: 2 }}
          >
            Save Step
          </Button>
        </Paper>
      )}
    </Box>
  );
};

export default ActionDefinitionBuilder;