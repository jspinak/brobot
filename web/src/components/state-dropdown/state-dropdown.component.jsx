import React from 'react';
import { useNavigate } from 'react-router-dom';

const StateDropdown = ({ states, currentStateId }) => {
    const navigate = useNavigate();

    const handleStateChange = (event) => {
        const selectedStateId = event.target.value;
        navigate(`/states/${selectedStateId}`);
    };

    return (
        <div className="state-dropdown">
            <label htmlFor="state-select">State: </label>
            <select id="state-select" value={currentStateId} onChange={handleStateChange}>
                {states.map(state => (
                    <option key={state.id} value={state.id}>
                        {state.id}: {state.name}
                    </option>
                ))}
            </select>
        </div>
    );
};

export default StateDropdown;