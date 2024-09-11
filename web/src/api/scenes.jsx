import axios from 'axios';

const API_URL = '${process.env.REACT_APP_BROBOT_API_URL}/api/scenes';

export const fetchAllScenes = async () => {
    try {
        const response = await axios.get(`${API_URL}/all`);
        return response.data;
    } catch (error) {
        console.error('Error fetching scenes:', error);
        throw error;
    }
};

// You can add more functions here if needed, for example:
export const fetchSceneById = async (id) => {
    try {
        const response = await axios.get(`${API_URL}/${id}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching scene with id ${id}:`, error);
        throw error;
    }
};
