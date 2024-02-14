import StateImage from '../state-image/state-image.component';
import './state-image-set.styles.css';

const StateImageSet = ({ stateImages }) => (
    <div className='state-image-set'>
        {stateImages.map((stateImage) => { 
            return <StateImage stateImage={stateImage} /> 
        })}
    </div>
);

export default StateImageSet;