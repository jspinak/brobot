import React, { useContext, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { ProjectContext } from '../../components/ProjectContext';
import api from './../../services/api'

const NavigationBar = () => {
    const [states, setStates] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const { selectedProject } = useContext(ProjectContext);
    const [firstStateId, setFirstStateId] = useState(null);

  useEffect(() => {
    if (!selectedProject) {
      setStates([]);
      setIsLoading(false);
      return;
    }

    const fetchStates = async () => {
      setIsLoading(true);
      try {
        console.log(`NavigationBar: Fetching states for project ${selectedProject.id}`);
        const response = await api.get(`/api/states/project/${selectedProject.id}`);
        if (response.status === 200) {
            setStates(response.data);
              if (response.data.length > 0) {
                setFirstStateId(response.data[0].id);
              }
            } else {
              throw new Error('Failed to fetch states');
            }
      } catch (error) {
        console.error('NavigationBar: Error fetching states:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchStates();
  }, [selectedProject]);

  if (isLoading) {
    return <div>Loading...</div>;
  }

return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          Brobot State Structure Builder
          {selectedProject && (
            <Typography variant="subtitle1" sx={{ mr: 2 }}>
              Current Project: {selectedProject.name}
            </Typography>
          )}
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Button color="inherit" component={Link} to="/">
            Home
          </Button>
          <Button color="inherit" component={Link} to="/states">
            All States
          </Button>
          <Button color="inherit" component={Link} to="/create-state">
            Create State
          </Button>
          {firstStateId && (
            <Button color="inherit" component={Link} to={`/states/${firstStateId}`}>
              Edit State
            </Button>
          )}
          <Button color="inherit" component={Link} to="/transition-graph">
            Transition Graph
          </Button>
          <Button color="inherit" component={Link} to="/debug">
            Debug Info
          </Button>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default NavigationBar;