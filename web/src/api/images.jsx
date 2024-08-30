import axios from 'axios';

const API_URL = 'http://localhost:8080/api/images';

export const fetchAllImages = async () => {
    try {
        const response = await axios.get(`${API_URL}/all`);
        return response.data;
    } catch (error) {
        console.error('Error fetching images:', error);
        throw error;
    }
};

// You can add more functions here if needed, for example:
export const fetchImageById = async (id) => {
    try {
        const response = await axios.get(`${API_URL}/${id}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching image with id ${id}:`, error);
        throw error;
    }
};
