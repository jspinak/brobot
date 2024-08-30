import React from 'react';
import './highlight.styles.css';

const Highlight = ({ imageRegion, adjustX, adjustY, scaleW, scaleH }) => {
    const { x, y, w, h } = imageRegion || {};
    const calculatedX = x * scaleW + adjustX;
    const calculatedY = y * scaleH + adjustY;
    const calculatedW = w * scaleW;
    const calculatedH = h * scaleH;

    // Don't render if we have invalid dimensions
    if (isNaN(calculatedX) || isNaN(calculatedY) || isNaN(calculatedW) || isNaN(calculatedH)) {
        console.error('Invalid highlight dimensions');
        return null;
    }

    return (
        <div className="highlight" style={{
            left: calculatedX,
            top: calculatedY,
            width: calculatedW,
            height: calculatedH,
        }} />
    );
};

export default Highlight;