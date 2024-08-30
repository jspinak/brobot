import { useState, useEffect } from 'react';
import { fetchAllImages } from '../api/images';

const useFetchImages = () => {
    const [images, setImages] = useState([]);

    useEffect(() => {
        const fetchImages = async () => {
            try {
                const data = await fetchAllImages();
                setImages(data);
            } catch (error) {
                console.error('Error fetching images:', error);
            }
        };

        fetchImages();
    }, []);

    return images;
};

export default useFetchImages;
