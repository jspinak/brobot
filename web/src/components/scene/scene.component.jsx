import React, {useEffect, useState, useCallback } from 'react';
import Image from './../image/image.component';
import Highlight from './../highlight/highlight.component';
import './scene.styles.css';

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
            const response = await fetch(`http://localhost:8080/api/scenes/${sceneId}`);
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
        return <div>Loading scene...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    if (!scene) {
        return <div>No scene data available</div>;
    }

    if (!scene || !scene.pattern || !scene.pattern.image) {
        return <div>Invalid scene data</div>;
    }

    return (
        <div key={scene.id} className="state-scene">
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
                        className="highlight"
                    />
                );
            })}
        </div>
    );
}

export default Scene;
