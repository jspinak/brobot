import { Routes, Route } from 'react-router-dom'
import Navigation from './routes/navigation/navigation.component'
import Home from './routes/home/home.component'
import AllStates from './routes/view-states/all-states.component';

const App = () => {
  return (
    <Routes>
      <Route path='/' element={<Navigation/>}>
        <Route index element={<Home />}/>
        <Route path='/states' element={<AllStates/>}/>
      </Route>
    </Routes>
  );
};

export default App;