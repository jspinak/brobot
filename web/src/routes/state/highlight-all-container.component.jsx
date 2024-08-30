import React from 'react';
import HighlightAllCheckbox from './highlight-all-checkbox.component';
import './highlight-all-container.styles.css';

const HighlightAllContainer = ({ isChecked, onChange }) => (
    <div className="highlight-all-container">
        <HighlightAllCheckbox isChecked={isChecked} onChange={onChange} />
        <label>Highlight All State Images</label>
    </div>
);

export default HighlightAllContainer;
