import React, { useState, useContext, useEffect } from 'react';
import { Typography, Container, Grid, Paper, Box, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField } from '@mui/material';
import { Link } from 'react-router-dom';
import ProjectSelector from './project-selector.component';
import CreateProjectDialog from './create-project-dialog.component';
import { ProjectContext } from './../../components/ProjectContext';
import api from './../../services/api'
import ScreenshotCapture from './ScreenshotCapture';

const Home = () => {
    const { selectedProject, setSelectedProject } = useContext(ProjectContext);
    const [openDialog, setOpenDialog] = useState(false);
    const [triggerRefresh, setTriggerRefresh] = useState(0);
    const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
    const [automationResult, setAutomationResult] = useState('');
    const [secondsToCapture, setSecondsToCapture] = useState('');
    const [captureFrequency, setCaptureFrequency] = useState('');
    const [automationDuration, setAutomationDuration] = useState('');
    const [isAutomationRunning, setIsAutomationRunning] = useState(false);

    useEffect(() => {
        console.log('Home: Current selected project:', selectedProject);
    }, [selectedProject]);

    const handleProjectChange = (projectId, projectName) => {
        console.log('Home: Project selected:', { id: projectId, name: projectName });
        setSelectedProject({ id: projectId, name: projectName });
    };

    const handleCreateProject = async (projectName) => {
        try {
            const response = await api.post(`/api/projects`, { name: projectName });
            console.log('Project created:', response.data);
            setSelectedProject(response.data);
            setOpenDialog(false);
            setTriggerRefresh(prev => prev + 1);
        } catch (error) {
            console.error('Error creating project:', error);
        }
    };

    const handleDeleteProject = async () => {
        if (!selectedProject) return;
        try {
            await api.delete(`/api/projects/${selectedProject.id}`);
            setSelectedProject(null);
            setTriggerRefresh(prev => prev + 1);
            setOpenDeleteDialog(false);
        } catch (error) {
            console.error('Error deleting project:', error);
        }
    };

    const handleTransferStateStructureToLibrary = async () => {
        if (!selectedProject) {
            setAutomationResult('No project selected');
            return;
        }
        try {
            setAutomationResult('Transferring state structure to library...');
            const response = await api.post(`/api/automation/save-to-library/${selectedProject.id}`);
            setAutomationResult(`${response.data.message} Project images have been synced.`);
        } catch (error) {
            setAutomationResult('Error: ' + (error.response?.data?.message || error.message));
        }
    };

    const runAutomation = async (endpoint) => {
        if (!selectedProject) {
            setAutomationResult('No project selected');
            return;
        }
        setAutomationResult('Running...');
        try {
            const response = await api.post(`/api/automation/${endpoint}`);
            setAutomationResult(response.data.message || response.data || 'Operation completed');
        } catch (error) {
            setAutomationResult('Error occurred: ' + error.message);
        }
    };

    const handleCaptureScreenshots = async () => {
        if (!selectedProject) {
            setAutomationResult('No project selected');
            return;
        }
        try {
            const response = await api.post(`/api/automation/capture-screenshots`, {
                secondsToCapture: parseInt(secondsToCapture, 10),
                captureFrequency: parseFloat(captureFrequency)
            });
            setAutomationResult(response.data.message || 'Screenshots captured successfully');
        } catch (error) {
            setAutomationResult('Error occurred: ' + error.message);
        }
    };

    const startTimedAutomation = async () => {
        if (!selectedProject) {
            setAutomationResult('No project selected');
            return;
        }

        const seconds = parseInt(automationDuration, 10);
        if (isNaN(seconds) || seconds <= 0) {
            setAutomationResult('Please enter a valid duration in seconds');
            return;
        }

        try {
            setAutomationResult('Starting timed automation...');
            const response = await api.post('/api/automation/start-timed', { seconds });
            setIsAutomationRunning(true);
            setAutomationResult(response.data.message);

            // Automatically update status when automation should complete
            setTimeout(() => {
                setIsAutomationRunning(false);
                setAutomationResult('Automation completed');
            }, seconds * 1000);
        } catch (error) {
            setAutomationResult('Error: ' + (error.response?.data?.message || error.message));
            setIsAutomationRunning(false);
        }
    };

    const stopAutomation = async () => {
        try {
            const response = await api.post('/api/automation/stop');
            setIsAutomationRunning(false);
            setAutomationResult(response.data.message);
        } catch (error) {
            setAutomationResult('Error stopping automation: ' + (error.response?.data?.message || error.message));
        }
    };

    return (
        <Container>
            <Box display="flex" alignItems="center" mt={6} mb={4}>
                <ProjectSelector
                    onProjectChange={handleProjectChange}
                    triggerRefresh={triggerRefresh}
                    style={{ width: '200px', marginRight: '10px' }}
                />
                {selectedProject && (
                    <>
                        <Typography variant="body1" sx={{ mx: 2 }}>
                            Selected Project: {selectedProject.name}
                        </Typography>
                        <Button
                            variant="contained"
                            color="secondary"
                            onClick={() => setOpenDeleteDialog(true)}
                        >
                            Delete Project
                        </Button>
                    </>
                )}
            </Box>

            <Box mb={4}>
                <Button
                    variant="contained"
                    color="primary"
                    onClick={() => setOpenDialog(true)}
                >
                    Create New Project
                </Button>
            </Box>

            <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                    <Paper elevation={3} sx={{ p: 3 }}>
                        <Typography variant="h5" gutterBottom>Project Management</Typography>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                            <Button variant="contained" color="primary" onClick={handleTransferStateStructureToLibrary} sx={{ mr: 1 }}>
                                Use Project for Automation
                            </Button>
                            <Button variant="contained" >
                                Save Project to Backup File
                            </Button>
                        </Box>
                    </Paper>
                </Grid>

                <Grid item xs={12} md={6}>
                    <Paper elevation={3} sx={{ p: 3 }}>
                        <Typography variant="h5" gutterBottom>Automation Controls</Typography>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                            <Button variant="contained" onClick={() => runAutomation('test-mouse-move')}>
                                Test Mouse Move
                            </Button>
                            <Button variant="contained" onClick={() => runAutomation('test-visit-images')}>
                                Test Visit Images
                            </Button>
                            <Button variant="contained" onClick={() => runAutomation('visit-all-states')}>
                                Visit All States
                            </Button>
                        </Box>
                    </Paper>
                    <Paper elevation={3} sx={{ p: 3 }}>
                        <Typography variant="h5" gutterBottom>Timed Automation</Typography>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                            <TextField
                                label="Duration (seconds)"
                                type="number"
                                value={automationDuration}
                                onChange={(e) => setAutomationDuration(e.target.value)}
                                disabled={isAutomationRunning}
                                sx={{ mb: 2 }}
                            />
                            <Button
                                variant="contained"
                                color="primary"
                                onClick={startTimedAutomation}
                                disabled={isAutomationRunning}
                            >
                                Start Timed Automation
                            </Button>
                            <Button
                                variant="contained"
                                color="secondary"
                                onClick={stopAutomation}
                                disabled={!isAutomationRunning}
                            >
                                Stop Automation
                            </Button>
                        </Box>
                    </Paper>
                </Grid>

            </Grid>

            <Grid container spacing={3} mt={3}>
                <Grid item xs={12}>
                    <ScreenshotCapture onResultChange={setAutomationResult} />
                </Grid>
            </Grid>

            {automationResult && (
                <Paper elevation={3} sx={{ mt: 3, p: 2 }}>
                    <Typography variant="h6" gutterBottom>Automation Result:</Typography>
                    <Typography>{automationResult}</Typography>
                </Paper>
            )}

            <CreateProjectDialog
                open={openDialog}
                onClose={() => setOpenDialog(false)}
                onCreateProject={handleCreateProject}
            />

            <Dialog
                open={openDeleteDialog}
                onClose={() => setOpenDeleteDialog(false)}
                aria-labelledby="alert-dialog-title"
                aria-describedby="alert-dialog-description"
            >
                <DialogTitle id="alert-dialog-title">Delete Project</DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        Are you sure you want to delete this project? This action cannot be undone.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDeleteDialog(false)}>Cancel</Button>
                    <Button onClick={handleDeleteProject} color="secondary" autoFocus>
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
}

export default Home;