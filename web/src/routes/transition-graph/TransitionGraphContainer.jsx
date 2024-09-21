import React, { useState, useEffect, useContext } from 'react';
import axios from 'axios';
import TransitionGraph from './TransitionGraph';
import { ProjectContext } from '../../components/ProjectContext';
import { CircularProgress, Typography } from '@mui/material';

const TransitionGraphContainer = () => {
  const [graphData, setGraphData] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const { selectedProject } = useContext(ProjectContext);

  useEffect(() => {
    const fetchGraphData = async () => {
      if (!selectedProject) {
        console.log('No project selected');
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      try {
        console.log(`Fetching graph data for project ID: ${selectedProject.id}`);
        const response = await axios.get(`${process.env.REACT_APP_BROBOT_API_URL}/api/transitions/graph-data/${selectedProject.id}`);
        console.log('API Response:', response);
        console.log('Received graph data:', response.data);

        if (response.data && response.data.states && response.data.transitions) {
          console.log('Valid graph data structure received');
          setGraphData(response.data);
          setError(null);
        } else {
          console.error('Invalid data structure received from API:', response.data);
          throw new Error('Invalid data structure received from API');
        }
      } catch (err) {
        console.error('Error fetching graph data:', err);
        setError(`Failed to fetch graph data: ${err.message}`);
        setGraphData(null);
      } finally {
        setIsLoading(false);
      }
    };

    fetchGraphData();
  }, [selectedProject]);

  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Typography color="error">{error}</Typography>;
  }

  if (!graphData) {
    return <Typography>No graph data available. Please select a project.</Typography>;
  }

  return <TransitionGraph data={graphData} />;
};

export default TransitionGraphContainer;