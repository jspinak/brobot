// hooks/useTransitions.jsx
import { useState, useEffect, useCallback } from 'react';
import api from '../services/api';

const useTransitions = (stateId) => {
  const [transitions, setTransitions] = useState([]);
  const [selectedTransition, setSelectedTransition] = useState(null);

  const fetchTransitions = useCallback(async () => {
    try {
      const response = await api.get('/api/transitions');
      if (response.status === 200) {
        setTransitions(response.data);
      } else {
        console.error('Failed to fetch transitions. Status:', response.status);
      }
    } catch (error) {
      console.error('Error fetching transitions:', error.message);
      if (error.response) {
        console.error('Response data:', error.response.data);
        console.error('Response status:', error.response.status);
      }
    }
  }, []);

  useEffect(() => {
    fetchTransitions();
  }, [fetchTransitions, stateId]);

  const handleTransitionSelect = useCallback((transition) => {
    setSelectedTransition(transition);
  }, []);

  const handleTransitionUpdate = useCallback(async (updatedTransition) => {
    try {
      const response = await api.put(`/api/transitions/${updatedTransition.id}`, updatedTransition);
      if (response.status === 200) {
        setTransitions(prevTransitions =>
          prevTransitions.map(t => t.id === updatedTransition.id ? updatedTransition : t)
        );
      }
    } catch (error) {
      console.error('Error updating transition:', error);
    }
  }, []);

  const handleTransitionDelete = useCallback(async (transitionId) => {
    try {
      const response = await api.delete(`/api/transitions/${transitionId}`);
      if (response.status === 204) {
        setTransitions(prevTransitions => prevTransitions.filter(t => t.id !== transitionId));
        setSelectedTransition(null);
      }
    } catch (error) {
      console.error('Error deleting transition:', error);
    }
  }, []);

  return {
    transitions,
    selectedTransition,
    handleTransitionSelect,
    handleTransitionUpdate,
    handleTransitionDelete,
    fetchTransitions
  };
};

export default useTransitions;