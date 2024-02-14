import { useState, useEffect } from 'react';

import ImageList from './components/image-list/image-list.component';
import PatternList from './components/pattern-list/pattern-list.component';
import StateList from './components/state-list/state-list.component';
import SearchBox from './components/search-box/search-box.component';
import './App.css';

const App = () => {
  const [searchField, setSearchField] = useState(''); // [value, setValue]
  const [images, setImages] = useState([]); 
  const [patterns, setPatterns] = useState([]);
  const [states, setStates] = useState([])
  const [filteredImages, setFilteredImages] = useState(images);
  const [filteredPatterns, setFilteredPatterns] = useState(patterns);
  const [filteredStates, setFilteredStates] = useState(states);

  useEffect(() => {
    fetch('http://localhost:8081/api/test/patterns/all')
      .then((response) => response.json())
      .then((patterns) => setPatterns(patterns));
  }, []);

  useEffect(() => {
    fetch('http://localhost:8081/api/test/images/all')
      .then((response) => response.json())
      .then((data) => setImages(data));
  }, []);

  useEffect(() => {
    fetch('http://localhost:8081/api/test/states/all')
      .then((response) => response.json())
      .then((data) => setStates(data));
  }, []);

  useEffect(() => {
    const newFilteredStates = states.filter((state) => {
      return state.name.toLocaleLowerCase().includes(searchField);
    });

    setFilteredStates(newFilteredStates);
  }, [states, searchField]);

  const onSearchChange = (event) => {
    const searchFieldString = event.target.value.toLocaleLowerCase();
    setSearchField(searchFieldString);
  };

  return (
    <div className="App">
      <h1 className='app-title'>States</h1>
      <SearchBox onChangeHandler={onSearchChange} placeholder='search states' className='search-box'/>
      <StateList states={filteredStates} />
    </div>
  );
};

export default App;
