import './image.styles.css';

const Image = ({ image, onLoad }) => {
    const { id, name, imageBase64 } = image;

    return (
    <div className='image' key={id}>
        <h1 className='image-name'>{name}</h1>
        <img
            src={`data:image/png;base64, ${imageBase64}`}
            alt={name}
            onLoad={onLoad}
        />
    </div>
)};

export default Image;