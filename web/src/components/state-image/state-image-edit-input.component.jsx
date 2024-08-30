import React from 'react';

const StateImageEditInput = ({ value, onChange }) => (
    <input
        type="text"
        value={value}
        onChange={onChange}
        className="state-image-edit-input"
    />
);

export default StateImageEditInput;
