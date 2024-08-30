import React from 'react';
import './image.styles.css';

const Image = ({ image, onLoad }) => {
    if (!image) {
        console.error('No image data provided');
        return <div className='image error'>Image data missing</div>;
    }

    const { id, name, imageBase64 } = image;

    if (!imageBase64) {
        console.error(`Image ${id} (${name}) has no base64 data`);
        return <div className='image error'>Image data incomplete</div>;
    }

    return (
        <div className='image' key={id}>
            <img
                src={`data:image/png;base64, ${imageBase64}`}
                alt={name}
                onLoad={onLoad}
                onError={(e) => {
                    console.error(`Error loading image ${id} (${name}):`, e);
                    e.target.onerror = null;
                    e.target.src = 'path/to/fallback/image.png';
                }}
            />
        </div>
    );
};

export default Image;