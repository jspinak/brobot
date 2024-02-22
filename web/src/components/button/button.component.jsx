import './button.styles.scss'

const BUTTON_TYPE_CLASSES = {
    inverted: 'inverted'
}

const Button = ({ children, buttonType, ...otherProps }) => {
    return (
        <button className={`button-container ${BUTTON_TYPE_CLASSES[buttonType]}`}>
            {children}
        </button>
    )
}

export default Button;