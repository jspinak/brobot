import React, { useState } from 'react';
import axios from 'axios';

function TestVisitImages() {
    const [isRunning, setIsRunning] = useState(false);
    const [result, setResult] = useState('');

    const runAutomation = async () => {
        setIsRunning(true);
        setResult('');
        try {
            const response = await axios.post('${process.env.REACT_APP_BROBOT_API_URL}/api/automation/test-visit-images');
            setResult(response.data);
        } catch (error) {
            setResult('Error occurred while running automation: ' + error.message);
        } finally {
            setIsRunning(false);
        }
    };

    return (
        <div>
            <button onClick={runAutomation} disabled={isRunning}>
                {isRunning ? 'Running...' : 'Test Visit Images'}
            </button>
            {result && <p>{result}</p>}
        </div>
    );
}

export default TestVisitImages;