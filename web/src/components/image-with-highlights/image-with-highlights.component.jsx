import React from 'react';
import Image from './../../components/image/image.component';
import Highlight from './../../components/highlight/highlight.component';

const ImageWithHighlights = ({ image, patterns, highlightAll, onImageLoad, calculateScaledPosition }) => {
  return (
    <div className="scene">
      <Image image={image} onLoad={onImageLoad} />
      {(highlightAll ? patterns : patterns).map((pattern) => {
        const { adjustX, adjustY, scaleW, scaleH } = calculateScaledPosition(pattern);
        return (
          <Highlight
            key={pattern.id}
            imageRegion={pattern.searchRegions.fixedRegion}
            adjustX={adjustX}
            adjustY={adjustY}
            scaleW={scaleW}
            scaleH={scaleH}
          />
        );
      })}
    </div>
  );
};

export default ImageWithHighlights;
