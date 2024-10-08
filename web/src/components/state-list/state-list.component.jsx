import State from '../../routes/state/state.component';

const StateList = ({ states }) => {
      console.log('StateList component rendered');
      console.log('States in StateList:', states);

  // Check if states is undefined or empty
  if (!states || states.length === 0) {
    return <div className='state-list'>No states found</div>;
  }

  return (
    <div className='state-list'>
      {states.map((state) => {
        if (!state || !state.id) {
          console.error('Invalid state object:', state);
          return null; // Skip rendering this item
        }
        return <State key={state.id} state={state} />;
      })}
    </div>
  );
};

export default StateList;