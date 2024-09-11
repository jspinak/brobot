import React from 'react';
import { Route, Routes } from 'react-router-dom';
import { Box, CircularProgress, Container } from '@mui/material';
import StateList from './../../components/state-list/state-list.component';
import StateDetails from './../state/state-details.component';

const AllStates = ({ states, isLoading }) => {
    console.log('AllStates component rendered');
    console.log('States:', states);
    console.log('Is Loading:', isLoading);

  return (
    <Container maxWidth="lg">
      <Box sx={{ textAlign: 'center' }}>
        <Routes>
          <Route
            path="/"
            element={
              isLoading ? (
                <CircularProgress />
              ) : (
                <StateList states={states} />
              )
            }
          />
          <Route path="/:stateId" element={<StateDetails allStates={states} isLoading={isLoading} />} />
        </Routes>
      </Box>
    </Container>
  );
};

export default AllStates;