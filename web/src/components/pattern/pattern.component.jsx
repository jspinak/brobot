import Image from '../image/image.component';
import './pattern.styles.css';

const Pattern = ({pattern}) => {
    const { id, name, url, imgpath, fixed, searchRegions, matchHistory, 
        dynamic, position, anchors, image } = pattern;

    // Extract fixedRegion coordinates
    const fixedRegion = searchRegions?.fixedRegion;

    return (
        <div className='pattern' key={id}>
            <h3 className="bold-heading"><strong>Pattern: </strong>{name}</h3>
            {fixedRegion && (
                <div className="fixed-region">
                    <p>Coordinates:</p>
                    <ul>
                        <li>x: {fixedRegion.x}</li>
                        <li>y: {fixedRegion.y}</li>
                        <li>width: {fixedRegion.w}</li>
                        <li>height: {fixedRegion.h}</li>
                    </ul>
                </div>
            )}
            {image && <Image image={image} />}
        </div>
    )
};

export default Pattern;

    