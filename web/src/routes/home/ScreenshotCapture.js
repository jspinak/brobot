// ScreenshotCapture.jsx
import React, { useState } from 'react';
import { Typography, Paper, Box, Button, TextField } from '@mui/material';
import api from '../../services/api';

const ScreenshotCapture = ({ onResultChange }) => {
    const [secondsToCapture, setSecondsToCapture] = useState('');
    const [captureFrequency, setCaptureFrequency] = useState('');
    const [recordingLocation, setRecordingLocation] = useState('');
    const [isCapturing, setIsCapturing] = useState(false);

    const handleCaptureScreenshots = async () => {
        if (!recordingLocation) {
            onResultChange('Please enter a recording location first');
            return;
        }

        try {
            // Set the recording location
            const locationResponse = await api.post(`/api/automation/set-recording-location`, {
                location: recordingLocation
            });

            if (locationResponse.data.status !== 'SUCCESS') {
                onResultChange('Failed to set recording location');
                return;
            }

            // Start the capture
            setIsCapturing(true);
            const response = await api.post(`/api/automation/capture-screenshots`, {
                secondsToCapture: parseInt(secondsToCapture, 10),
                captureFrequency: parseFloat(captureFrequency)
            });
            onResultChange(response.data.message || 'Screenshots captured successfully');
        } catch (error) {
            onResultChange('Error occurred: ' + error.message);
            setIsCapturing(false);
        }
    };

    const handleStopCapture = async () => {
        try {
            const response = await api.post(`/api/automation/stop-capture-screenshots`);
            onResultChange(response.data.message || 'Screenshot capture stopped successfully');
            setIsCapturing(false);
        } catch (error) {
            onResultChange('Error stopping capture: ' + error.message);
        }
    };

    return (
        <Paper elevation={3} sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>Capture Screenshots</Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <TextField
                    label="Recording Location"
                    value={recordingLocation}
                    onChange={(e) => setRecordingLocation(e.target.value)}
                    fullWidth
                    helperText="Enter the absolute path where recordings will be saved (e.g., C:\Screenshots or /home/user/screenshots)"
                />
                <TextField
                    label="Seconds to Capture"
                    type="number"
                    value={secondsToCapture}
                    onChange={(e) => setSecondsToCapture(e.target.value)}
                    fullWidth
                />
                <TextField
                    label="Capture Frequency"
                    type="number"
                    value={captureFrequency}
                    onChange={(e) => setCaptureFrequency(e.target.value)}
                    fullWidth
                />
                {!isCapturing ? (
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleCaptureScreenshots}
                        disabled={!recordingLocation}
                    >
                        Start Capture
                    </Button>
                ) : (
                    <Button
                        variant="contained"
                        color="secondary"
                        onClick={handleStopCapture}
                    >
                        Stop Capture
                    </Button>
                )}
            </Box>
        </Paper>
    );
};

export default ScreenshotCapture;