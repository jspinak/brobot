import State from '../state/state.component';
import './state-list.styles.css';

const StateList = ({ states }) => (
    <div className='state-list'>
        {states.map((state) => { 
            return <State state={state} /> 
        })}
    </div>
);

export default StateList;