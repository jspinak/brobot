import PatternList from '../pattern-list/pattern-list.component';
import './state-image.styles.css';

const StateImage = ({stateImage}) => {
    const { id, name, patterns, ownerStateName, timesActedOn, shared } = stateImage;

    return (
        <div className='state-image' key={id}>
            <h1 className="bold-heading"><strong>State Image</strong></h1>
            <h1>name: {name}</h1>
            <h1>ownerStateName: {ownerStateName}</h1>
            <PatternList patterns={patterns}/>
        </div>
    )
};

export default StateImage;