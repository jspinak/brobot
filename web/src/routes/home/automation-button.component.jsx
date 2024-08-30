// src/components/AutomationButton.js
import React, { useState } from 'react';
import axios from 'axios';

function AutomationButton() {
    const [isRunning, setIsRunning] = useState(false);
    const [result, setResult] = useState('');

    const runAutomation = async () => {
        setIsRunning(true);
        setResult('');
        try {
            const response = await axios.post('/api/automation/run-transition-test');
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
                {isRunning ? 'Running...' : 'Test All Transitions'}
            </button>
            {result && <p>{result}</p>}
        </div>
    );
}

export default AutomationButton;