import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Route, Routes } from 'react-router-dom';
import { Outlet } from 'react-router-dom'

import ImageList from './../../components/image-list/image-list.component';
import PatternList from './../../components/pattern-list/pattern-list.component';
import StateList from './../../components/state-list/state-list.component';
import StateDetails from './../../components/state/state-details.component';
import SearchBox from './../../components/search-box/search-box.component';
import './all-states.styles.css';

const AllStates = () => {
  const [searchField, setSearchField] = useState(''); // [value, setValue]
  const [images, setImages] = useState([]); 
  const [patterns, setPatterns] = useState([]);
  const [states, setStates] = useState([])
  const [filteredImages, setFilteredImages] = useState(images);
  const [filteredPatterns, setFilteredPatterns] = useState(patterns);
  const [filteredStates, setFilteredStates] = useState(states);
  const [isLoading, setIsLoading] = useState(true);

useEffect(() => {
    setIsLoading(true);
    fetch('http://localhost:8080/api/states/all')
      .then((response) => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then((data) => {
        setStates(data);
        setIsLoading(false);
      })
      .catch((error) => {
        console.error('Error fetching states:', error);
        setIsLoading(false);
        // Handle error here
      });
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
    <div className="AllStates">
      <h1 className='app-title'>States</h1>
      <SearchBox onChangeHandler={onSearchChange} placeholder='search states' className='search-box'/>
      <Routes>
        <Route path="/" element={
          isLoading ? (
            <div>Loading...</div>
          ) : (
            <StateList states={filteredStates} />
          )
        } />
        <Route path="/:stateId" element={<StateDetails states={states} />} />
      </Routes>
    </div>
  );
};

export default AllStates;
