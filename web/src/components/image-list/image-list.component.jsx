import React, { useState, useEffect } from 'react';
import { Box, Typography, Grid, CircularProgress } from '@mui/material';
import Image from '../image/image.component';
import api from './../../services/api';

const ImageList = ({ imageIds = [], title, className = '' }) => {
    const [images, setImages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    console.log('Received imageIds:', imageIds);

    useEffect(() => {
        const fetchImages = async () => {
            setLoading(true);
            setError(null);
            try {
                console.log('Fetching images:', imageIds);
                const imagePromises = imageIds.map(id => api.get(`/api/images/${id}`));
                const imageResponses = await Promise.all(imagePromises);
                console.log('Image responses:', imageResponses);
                const fetchedImages = imageResponses.map(response => response.data);
                console.log('Fetched images:', fetchedImages);
                setImages(fetchedImages);
            } catch (err) {
                console.error('Error fetching images:', err);
                setError('Failed to load images');
            } finally {
                setLoading(false);
            }
        };

        if (imageIds.length > 0) {
            fetchImages();
        } else {
            setLoading(false);
        }
    }, [imageIds]);

    if (loading) {
        return <CircularProgress />;
    }

    if (error) {
        return <Typography color="error">{error}</Typography>;
    }

    return (
        <Box sx={{ mb: 1, border: 1, borderColor: 'grey.300', borderRadius: 1, p: 0.5, textAlign: 'center' }}>
            <Typography variant="h6" component="h2" gutterBottom>
                <strong>{title}</strong>
            </Typography>
            <Grid container spacing={0.5} sx={{ backgroundColor: 'grey.300', p: 0.5, borderRadius: 0.5 }}>
                {images.length > 0 ? (
                    images.map((image, index) => (
                        <Grid item xs={6} sm={4} md={3} lg={2} key={image.id || index}>
                            <Image image={image}     />
                        </Grid>
                    ))
                ) : (
                    <Grid item xs={12}>
                        <Typography>No images available</Typography>
                    </Grid>
                )}
            </Grid>
        </Box>
    );
};

export default ImageList;