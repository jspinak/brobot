import Image from '../image/image.component';
import './image-list.styles.css';

const ImageList = ({ images, title }) => (
    <div className='image-list'>
        <h1><strong>{title}</strong></h1>
        {images.map((image) => { 
            return <Image image={image} /> 
        })}
    </div>
);

export default ImageList;