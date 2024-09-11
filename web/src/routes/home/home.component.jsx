import React, { useState, useContext, useEffect } from 'react';
import { Typography, Container, Grid, Paper, Box, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField } from '@mui/material';
import { Link } from 'react-router-dom';
import axios from 'axios';
import ProjectSelector from './project-selector.component';
import CreateProjectDialog from './create-project-dialog.component';
import { ProjectContext } from './../../components/ProjectContext';

const Home = () => {
    const { selectedProject, setSelectedProject } = useContext(ProjectContext);
    const [openDialog, setOpenDialog] = useState(false);
    const [triggerRefresh, setTriggerRefresh] = useState(0);
    const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
    const [automationResult, setAutomationResult] = useState('');

    useEffect(() => {
        console.log('Home: Current selected project:', selectedProject);
    }, [selectedProject]);

    const handleProjectChange = (projectId, projectName) => {
        console.log('Home: Project selected:', { id: projectId, name: projectName });
        setSelectedProject({ id: projectId, name: projectName });
    };

    const handleCreateProject = async (projectName) => {
        try {
            const response = await axios.post('${process.env.REACT_APP_BROBOT_API_URL}/api/projects', { name: projectName });
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
            await axios.delete(`${process.env.REACT_APP_BROBOT_API_URL}/api/projects/${selectedProject.id}`);
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
            const response = await axios.post(`${process.env.REACT_APP_BROBOT_API_URL}/api/automation/save-to-library/${selectedProject.id}`);
            setAutomationResult(response.data);
        } catch (error) {
            setAutomationResult('Error saving to library: ' + error.message);
        }
    };

    const runAutomation = async (endpoint) => {
        if (!selectedProject) {
            setAutomationResult('No project selected');
            return;
        }
        setAutomationResult('Running...');
        try {
            const response = await axios.post(`${process.env.REACT_APP_BROBOT_API_URL}/api/automation/${endpoint}`);
            setAutomationResult(response.data);
        } catch (error) {
            setAutomationResult('Error occurred: ' + error.message);
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