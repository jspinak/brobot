//import './string-list.styles.css';

const StringList = ({ strings, title }) => (
    <div className='string-list'>
        <h1><strong>{title}: </strong></h1>
        {strings.map((string) => { 
            return <div>{string}, </div>
        })}
    </div>
);

export default StringList;