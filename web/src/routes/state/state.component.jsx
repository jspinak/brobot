import React from 'react';
import ImageList from '../../components/image-list/image-list.component';
import Pattern from '../../components/pattern/pattern.component';
import StateImageSet from '../../components/state-image-set/state-image-set.component';
import StringList from '../../components/helper-functions/string-list.component';
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
                  <strong>State </strong>{name}
                </Link>
            </h1>
            <h1>id: {id}</h1>
            <ImageList images={scenes} title="Scenes" className="full-width-list" />
            <ImageList images={images} title="Images" className="two-column-list" />
        </div>
    )
};

export default State;