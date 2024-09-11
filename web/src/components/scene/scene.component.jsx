import React, { useEffect, useState, useCallback } from 'react';
import { Box, CircularProgress, Typography } from '@mui/material';
import Image from './../image/image.component';
import Highlight from './../highlight/highlight.component';

const sceneCache = new Map();

const Scene = ({ sceneId, highlightAll, state, highlightedPatterns, handleImageLoad, calculateScaledPosition }) => {
    const [scene, setScene] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    console.log('Current state:', state);
    console.log('Scene Id:', sceneId);

    const fetchScene = useCallback(async () => {
        if (sceneCache.has(sceneId)) {
            setScene(sceneCache.get(sceneId));
            setIsLoading(false);
            return;
        }

        setIsLoading(true);
        try {
            const response = await fetch(`${process.env.REACT_APP_BROBOT_API_URL}/api/scenes/${sceneId}`);
            if (!response.ok) {
                throw new Error(`Error fetching scene:${sceneId}`);
            }
            const data = await response.json();
            sceneCache.set(sceneId, data);
            setScene(data);
            setIsLoading(false);
        } catch (err) {
            console.error('Error fetching scene:', err);
            setError(err.message);
            setIsLoading(false);
        }
    }, [sceneId]);

    useEffect(() => {
        fetchScene();
    }, [fetchScene]);

    if (isLoading) {
        return <CircularProgress />;
    }

    if (error) {
        return <Typography color="error">Error: {error}</Typography>;
    }

    if (!scene) {
        return <Typography>No scene data available</Typography>;
    }

    if (!scene || !scene.pattern || !scene.pattern.image) {
        return <Typography>Invalid scene data</Typography>;
    }

    return (
        <Box sx={{ position: 'relative', mb: 2.5, overflow: 'hidden' }}>
            <Image
                image={scene.pattern.image}
                onLoad={(event) => handleImageLoad(event, scene.id)}
            />
            {(highlightAll ? state.stateImages.flatMap(si => si.patterns) : highlightedPatterns).map((pattern) => {
                if (!pattern || !pattern.image) {
                    console.error('Invalid pattern:', pattern);
                    return null;
                }
                const { adjustX, adjustY, scaleW, scaleH } = calculateScaledPosition(pattern, scene.id);
                console.log('Pattern being highlighted:', pattern);
                return (
                    <Highlight
                        key={pattern.image.id}
                        imageRegion={pattern.searchRegions.fixedRegion}
                        adjustX={adjustX}
                        adjustY={adjustY}
                        scaleW={scaleW}
                        scaleH={scaleH}
                        sx={{
                            border: 2,
                            borderColor: 'rgba(255, 255, 255, 0.8)',
                            boxShadow: '0 0 0 2px rgba(0, 0, 0, 0.8)',
                            backgroundColor: 'rgba(255, 0, 0, 0.3)',
                            position: 'absolute',
                            pointerEvents: 'none',
                            zIndex: 10
                        }}
                    />
                );
            })}
        </Box>
    );
}

export default Scene;