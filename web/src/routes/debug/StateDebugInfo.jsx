import React, { useState, useEffect } from 'react';
import { Paper, Typography, Box, Button } from '@mui/material';
import api from './../../services/api'

const StateDebugInfo = () => {
  const [debugInfo, setDebugInfo] = useState({
    stateCount: 0,
    stateNames: [],
    transitionCount: 0
  });
  const [loading, setLoading] = useState(false);

  const fetchDebugInfo = async () => {
    setLoading(true);
    try {
      const { data } = await api.get(`/api/debug/brobot/states`);
      setDebugInfo(data || { stateCount: 0, stateNames: [], transitionCount: 0 });
      console.log('Debug info:', data);
    } catch (error) {
      console.error('Error fetching debug info:', error);
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchDebugInfo();
  }, []);

  return (
    <Paper sx={{ p: 2, mb: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">Brobot Library Debug Info</Typography>
        <Button
          variant="outlined"
          onClick={fetchDebugInfo}
          disabled={loading}
        >
          Refresh
        </Button>
      </Box>
      <Typography>States in Library: {debugInfo?.stateCount || 0}</Typography>
      <Typography>Transitions in Library: {debugInfo?.transitionCount || 0}</Typography>
      {debugInfo?.stateNames?.length > 0 && (
        <Box mt={2}>
          <Typography variant="subtitle2">State Names:</Typography>
          {debugInfo.stateNames.map(name => (
            <Typography key={name} sx={{ ml: 2 }}>{name}</Typography>
          ))}
        </Box>
      )}
    </Paper>
  );
};

export default StateDebugInfo;