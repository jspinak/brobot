import StateImage from '../state-image/state-image.component';
import './state-image-set.styles.css';

const StateImageSet = ({ stateImages = [], onHover, onMouseLeave, onClick }) => {
  console.log('StateImageSet props:', { stateImages, onHover, onClick });
  return (
    <div className='state-image-set'>
      {stateImages.map((stateImage) => (
        <div
          key={stateImage.id}
          className="state-image-wrapper"
          onMouseEnter={() => {
            console.log('Mouse enter, calling onHover with:', stateImage);
            onHover(stateImage);
          }}
          onMouseLeave={onMouseLeave}
          onClick={() => onClick(stateImage)}
        >
          <StateImage stateImage={stateImage} />
        </div>
      ))}
    </div>
  );
};

export default StateImageSet;