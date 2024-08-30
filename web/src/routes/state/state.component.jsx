import React from 'react';
import ImageList from '../../components/image-list/image-list.component';
import SceneImageList from '../../components/scene/scene-image-list.component';
import { Link } from 'react-router-dom';
import './state.styles.css';

const State = ({state}) => {
    const { id, name, stateText, stateImages, stateStrings, stateRegions, stateLocations,
    blocking, canHide, hidden, pathScore, lastAccessed, baseProbabilityExists,
    timesVisited, scenes } = state;

    const images = stateImages.flatMap(stateImage =>
      stateImage.patterns.map(pattern => pattern.image)
    );

    return (
        <div className='state' key={id}>
            <h1 className="bold-heading">
                <Link to={`/states/${id}`}>
                  {name}
                </Link>
            </h1>
            <h1>id: {id}</h1>
            <SceneImageList scenes={scenes} title="Scenes" className="full-width-list" />
            <ImageList images={images} title="Images" className="two-column-list" />
        </div>
    )
};

export default State;