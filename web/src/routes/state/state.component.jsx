import React from 'react';
import { Link } from 'react-router-dom';
import { Box, Typography, Paper } from '@mui/material';
import ImageList from '../../components/image-list/image-list.component';
import SceneImageList from '../../components/scene/scene-image-list.component';

const State = ({ state }) => {
  const { id, name, stateImages, scenes } = state;

  const images = stateImages.flatMap(stateImage =>
    stateImage.patterns.map(pattern => pattern.image)
  );

  return (
    <Paper
      elevation={3}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        p: 2,
        m: 2,
        backgroundColor: 'grey.200',
        '&:hover': {
          backgroundColor: 'grey.300',
        },
      }}
    >
      <Typography variant="h5" component={Link} to={`/states/${id}`} sx={{ fontWeight: 'bold', mb: 1 }}>
        {name}
      </Typography>
      <Typography variant="body1">ID: {id}</Typography>
      <Box sx={{ mt: 2 }}>
        <SceneImageList scenes={scenes} title="Scenes" />
      </Box>
      <Box sx={{ mt: 2 }}>
        <ImageList images={images} title="Images" />
      </Box>
    </Paper>
  );
};

export default State;