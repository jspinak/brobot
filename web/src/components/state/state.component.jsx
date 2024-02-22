import ImageList from '../image-list/image-list.component';
import Pattern from '../pattern/pattern.component';
import StateImageSet from '../state-image-set/state-image-set.component';
import StringList from '../helper-functions/string-list.component';
import './state.styles.css';

const State = ({state}) => {
    const { id, name, stateText, stateImages, stateStrings, stateRegions, stateLocations,
    blocking, canHide, hidden, pathScore, lastAccessed, baseProbabilityExists,
    timesVisited, scenes, illustrations } = state;

    return (
        <div className='state' key={id}>
            <h1 className="bold-heading"><strong>State </strong>{name}</h1>
            <h1>id: {id}</h1>
            <ImageList images={scenes} title="scenes"/>
            <ImageList images={illustrations} title={"illustrations"}/>
            <StringList strings={canHide} title={"can hide"}/>
            <StateImageSet stateImages={stateImages} />
        </div>
    )
};

export default State;