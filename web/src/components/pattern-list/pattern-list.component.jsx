import Pattern from '../pattern/pattern.component';
import './pattern-list.styles.css';

const PatternList = ({ patterns  = []}) => (
    <div className='pattern-list'>
        {patterns.map((pattern) => { 
            return <Pattern key={pattern.id} pattern={pattern} />
        })}
    </div>
);

export default PatternList;