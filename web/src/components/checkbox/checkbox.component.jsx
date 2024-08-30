import React from 'react';

const StateImageCheckbox = ({ checked, onChange }) => (
    <input
        type="checkbox"
        checked={checked}
        onChange={onChange}
        className="state-image-checkbox"
    />
);

export default StateImageCheckbox;
