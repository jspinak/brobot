import React from 'react';
import ImageList from './../image-list/image-list.component';

const SceneImageList = ({ scenes, title = "Scenes", className = "full-width-list" }) => {
    // Check if scenes is undefined or empty
    if (!scenes || scenes.length === 0) {
        return <div>No scenes found</div>;
    }

    // Extract the pattern.image from each scene
    const sceneImages = scenes.map(scene => scene.pattern.image);

    return (
        <ImageList
            images={sceneImages}
            title={title}
            className={className}
        />
    );
};

export default SceneImageList;