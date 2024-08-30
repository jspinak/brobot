import React from 'react';
import StateDropdown from "../../components/state-dropdown/state-dropdown.component";
import HighlightAllContainer from './highlight-all-container.component';
import './state-details-buttons.styles.css';

const StateDetailsButtons = ({
                                 isChecked,
                                 onCheckboxChange,
                                 states,
                                 currentStateId,
                                 currentStateName,
                                 onStateNameChange,
                                 onStateNameSubmit,
                                 stateImageCount,
                                 transitionCount
                             }) => (
    <div className="state-details-buttons">
        <div className="state-dropdown-container common-height">
            <StateDropdown states={states} currentStateId={currentStateId} className="common-height"/>
        </div>
        <HighlightAllContainer isChecked={isChecked} onChange={onCheckboxChange} />
        <div className="state-name-edit common-height">
            <input
                type="text"
                value={currentStateName}
                onChange={(e) => onStateNameChange(e.target.value)}
                placeholder="State Name"
            />
            <button onClick={onStateNameSubmit}>Update Name</button>
        </div>
        <div className="state-stats common-height">
            <span>Images: {stateImageCount}</span>
            <span>Transitions: {transitionCount}</span>
        </div>
    </div>
);

export default StateDetailsButtons;