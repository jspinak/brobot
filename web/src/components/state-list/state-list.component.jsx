import State from '../state/state.component';
import './state-list.styles.css';

const StateList = ({ states }) => (
    <div className='state-list'>
        {states.map((state) => {
            console.log(state.id); // Debugging: check if id is unique and defined
            return <State key={state.id} state={state} />
        })}
    </div>
);

export default StateList;