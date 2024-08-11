import Image from '../image/image.component';
import './image-list.styles.css';

const ImageList = ({ images = [], title, className = '' }) => (
    <div className={`image-list-container ${className}`}>
        <h1><strong>{title}</strong></h1>
        <div className='image-list'>
            {images.map((image) => {
                return <Image key={image.id} image={image} />
            })}
        </div>
    </div>
);

export default ImageList;