import React, { useState } from 'react';
import StateDropdown from "../../components/state-dropdown/state-dropdown.component";
import HighlightAllContainer from './highlight-all-container.component';
import {
  Box,
  Button,
  TextField,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Typography
} from '@mui/material';
import api from './../../services/api'

const StateDetailsButtons = ({
  isChecked,
  onCheckboxChange,
  states,
  currentStateId,
  currentStateName,
  onStateNameChange,
  onStateNameSubmit,
  stateImageCount,
  transitionCount,
  onStateDeleted
}) => {
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);

  const handleDeleteClick = () => {
    setShowConfirmDialog(true);
  };

const handleConfirmDelete = async () => {
  try {
    console.log('Deleting state with ID:', currentStateId); // Add this log
    await api.delete(`/api/states/${currentStateId}`);
    onStateDeleted(currentStateId);
    setShowConfirmDialog(false); // Close the dialog after successful deletion
  } catch (error) {
    console.error('Error deleting state:', error);

    if (error.response) {
      console.error('Error response:', error.response); // Add this log
      if (error.response.status === 404) {
        alert('The state you are trying to delete no longer exists.');
      } else if (error.response.status === 500) {
        alert(`An error occurred while deleting the state: ${error.response.data}`);
      } else {
        alert('An unexpected error occurred. Please try again.');
      }
    } else if (error.request) {
      alert('No response received from the server. Please check your connection and try again.');
    } else {
      alert('An error occurred while setting up the request. Please try again.');
    }
  }
};

  const handleCancelDelete = () => {
    setShowConfirmDialog(false);
  };

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, my: 2 }}>
      <StateDropdown states={states || []} currentStateId={currentStateId} />
      <HighlightAllContainer isChecked={isChecked} onChange={onCheckboxChange} />
      <Box sx={{ display: 'flex', alignItems: 'center' }}>
        <TextField
          value={currentStateName}
          onChange={(e) => onStateNameChange(e.target.value)}
          placeholder="State Name"
          size="small"
        />
        <Button onClick={onStateNameSubmit} variant="contained" sx={{ ml: 1 }}>
          Update Name
        </Button>
      </Box>
      <Button onClick={handleDeleteClick} variant="contained" color="error">
        Delete State
      </Button>
      <Box sx={{ display: 'flex', gap: 2 }}>
        <Typography variant="body2">Images: {stateImageCount}</Typography>
        <Typography variant="body2">Transitions: {transitionCount}</Typography>
      </Box>
      <Dialog
        open={showConfirmDialog}
        onClose={handleCancelDelete}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle id="alert-dialog-title">{"Confirm Delete"}</DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            Are you sure you want to delete this state?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelDelete}>Cancel</Button>
          <Button onClick={handleConfirmDelete} autoFocus>
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default StateDetailsButtons;