import React, { useContext, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { ProjectContext } from '../../components/ProjectContext';

const NavigationBar = () => {
  const { selectedProject } = useContext(ProjectContext);
  const [firstStateId, setFirstStateId] = useState(null);

  useEffect(() => {
    fetch('${process.env.REACT_APP_BROBOT_API_URL}/api/states/all', {
          headers: {
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache',
            'Expires': '0'
          }
        })
      .then(response => response.json())
      .then(states => {
        if (states.length > 0) {
          setFirstStateId(states[0].id);
        }
      })
      .catch(error => console.error('Error fetching states:', error));
  }, []);

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
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default NavigationBar;