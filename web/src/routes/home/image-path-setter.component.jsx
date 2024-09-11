import React, { useState } from 'react';
import axios from 'axios';

function ImagePathSetter() {
    const [imagePath, setImagePath] = useState('');
    const [result, setResult] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setResult('');
        try {
            const response = await axios.post('${process.env.REACT_APP_BROBOT_API_URL}/api/automation/set-image-path', { imagePath });
            setResult(response.data);
        } catch (error) {
            setResult('Error occurred while setting image path: ' + error.message);
        }
    };

    return (
        <div>
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    value={imagePath}
                    onChange={(e) => setImagePath(e.target.value)}
                    placeholder="Enter image path"
                />
                <button type="submit">Set Image Path</button>
            </form>
            {result && <p>{result}</p>}
        </div>
    );
}

export default ImagePathSetter;