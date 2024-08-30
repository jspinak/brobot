import React from 'react';
import StateImage from './../state-image/state-image.component';
import StateImageOptions from './../state-image/state-image-options.component';

const StateImageSet = ({
                           stateImages,
                           transitions,
                           onHover,
                           onMouseLeave,
                           onClick,
                           selectedStateImage,
                           allStates,
                           onCreateTransition,
                           onNameChange,
                           onDelete,
                           onMove,
                           onStateImageUpdate
                       }) => {
    return (
        <>
            {stateImages.map((stateImage) => (
                <div key={stateImage.id} className="state-image-wrapper">
                    <div className="state-image-container">
                        <StateImage
                            stateImage={stateImage}
                            transitions={transitions}
                            allStates={allStates}
                            onHover={() => onHover(stateImage)}
                            onMouseLeave={onMouseLeave}
                            onClick={() => onClick(stateImage)}
                            isSelected={selectedStateImage && selectedStateImage.id === stateImage.id}
                        />
                    </div>
                    {selectedStateImage && selectedStateImage.id === stateImage.id && (
                        <StateImageOptions
                            stateImage={stateImage}
                            allStates={allStates}
                            transitions={transitions}
                            onCreateTransition={onCreateTransition}
                            onNameChange={onNameChange}
                            onDelete={onDelete}
                            onMove={onMove}
                            onStateImageUpdate={onStateImageUpdate}
                        />
                    )}
                </div>
            ))}
        </>
    );
};

export default StateImageSet;