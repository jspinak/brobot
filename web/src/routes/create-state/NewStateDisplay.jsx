import { Box, Typography, List, ListItem, ListItemText, ListItemAvatar, Avatar } from '@mui/material';

const NewStateDisplay = ({ newState }) => {
  if (!newState) return null;

  return (
    <Box sx={{ mt: 4 }}>
      <Typography variant="h5" gutterBottom>
        New State Created
      </Typography>
      <Typography variant="body1">
        <strong>Name:</strong> {newState.name}
      </Typography>
      <Typography variant="h6" sx={{ mt: 2 }}>
        Patterns:
      </Typography>
      <List>
        {newState.stateImages.map((stateImage, index) => {
          const pattern = stateImage.patterns[0];
          return (
            <ListItem key={index}>
              <ListItemAvatar>
                <Avatar
                  variant="square"
                  src={`data:image/png;base64, ${pattern.image.imageBase64}`}
                  alt={pattern.name}
                  sx={{ width: 56, height: 56 }}
                />
              </ListItemAvatar>
              <ListItemText primary={pattern.name} />
            </ListItem>
          );
        })}
      </List>
    </Box>
  );
};

export default NewStateDisplay;
