import React from 'react';
import { Box, Typography, Grid } from '@mui/material';
import Image from '../image/image.component';

const ImageList = ({ images = [], title, className = '' }) => (
    <Box sx={{ mb: 1, border: 1, borderColor: 'grey.300', borderRadius: 1, p: 0.5, textAlign: 'center' }}>
        <Typography variant="h6" component="h2" gutterBottom>
            <strong>{title}</strong>
        </Typography>
        <Grid container spacing={0.5} sx={{ backgroundColor: 'grey.300', p: 0.5, borderRadius: 0.5 }}>
            {images.map((image) => (
                <Grid item xs={6} sm={4} md={3} lg={2} key={image.id}>
                    <Image image={image} />
                </Grid>
            ))}
        </Grid>
    </Box>
);

export default ImageList;