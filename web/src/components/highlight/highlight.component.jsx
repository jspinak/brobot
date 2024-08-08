import React from 'react';
import './highlight.styles.css';

const Highlight = ({ image, adjustX, adjustY, scaleW, scaleH }) => {
  const { x, y, w, h } = image;

  const calculatedX = x * scaleW + adjustX;
  const calculatedY = y * scaleH + adjustY;
  const calculatedW = w * scaleW;
  const calculatedH = h * scaleH;

  console.log('image dimensions:', { x: x, y: y, w: w, h: h });
  console.log('Highlight dimensions:', { x: calculatedX, y: calculatedY, w: calculatedW, h: calculatedH });

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

