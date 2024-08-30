import React from 'react';

const HighlightAllCheckbox = ({ isChecked, onChange }) => {
  return (
    <div className="highlight-all-checkbox">
      <label>
        <input
          type="checkbox"
          checked={isChecked}
          onChange={onChange}
        />
      </label>
    </div>
  );
};

export default HighlightAllCheckbox;
