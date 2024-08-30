import React, { useState } from 'react';
import axios from 'axios';

function TestMouseMove() {
    const [isRunning, setIsRunning] = useState(false);
    const [result, setResult] = useState('');

    const runAutomation = async () => {
        setIsRunning(true);
        setResult('');
        try {
            const response = await axios.post('http://localhost:8080/api/automation/test-mouse-move');
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
                {isRunning ? 'Running...' : 'Test Mouse Move'}
            </button>
            {result && <p>{result}</p>}
        </div>
    );
}

export default TestMouseMove;