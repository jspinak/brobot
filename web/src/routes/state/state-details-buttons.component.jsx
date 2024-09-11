import React, { useState } from 'react';
import axios from 'axios';
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
      await axios.delete(`${process.env.REACT_APP_BROBOT_API_URL}/api/states/${currentStateId}`);
      setShowConfirmDialog(false);
      onStateDeleted(currentStateId);
    } catch (error) {
      console.error('Error deleting state:', error);
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