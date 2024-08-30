import { useState, useEffect } from 'react';
import { fetchAllScenes } from '../api/scenes';

const useFetchScenes = () => {
    const [scenes, setScenes] = useState([]);

    useEffect(() => {
        const fetchScenes = async () => {
            try {
                const data = await fetchAllScenes();
                setScenes(data);
            } catch (error) {
                console.error('Error fetching scenes:', error);
            }
        };

        fetchScenes();
    }, []);

    return scenes;
};

export default useFetchScenes;
