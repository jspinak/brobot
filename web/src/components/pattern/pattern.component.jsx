import Image from '../image/image.component';
import './pattern.styles.css';

const Pattern = ({pattern}) => {
    const { id, name, url, imgpath, fixed, searchRegions, matchHistory, 
        dynamic, position, anchors, image } = pattern;

    // Extract fixedRegion coordinates
    const fixedRegion = searchRegions?.fixedRegion;

    return (
        <div className='pattern' key={id}>
            {/*<h3 className="bold-heading">{name}</h3>
            {fixedRegion && (
                <div className="fixed-region">
                </div>
            )}*/}
            {image && <Image image={image} />}
        </div>
    )
};

export default Pattern;

    