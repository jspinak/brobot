import Image from '../image/image.component';
import './pattern.styles.css';

const Pattern = ({pattern}) => {
    const { id, name, url, imgpath, fixed, searchRegions, matchHistory, 
        dynamic, position, anchors, image } = pattern;

    return (
        <div className='pattern' key={id}>
            <h1 className="bold-heading"><strong>Pattern</strong></h1>
            <h1>name: {name}</h1>
            <h1>path: {imgpath}</h1>
            <h1>fixed: {fixed.toString()}</h1>
            <Image image={image} />
        </div>
    )
};

export default Pattern;

    