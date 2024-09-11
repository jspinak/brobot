import React, { createContext, useState, useEffect } from 'react';

export const ProjectContext = createContext();

export const ProjectProvider = ({ children }) => {
  const [selectedProject, setSelectedProject] = useState(() => {
    const stored = localStorage.getItem('selectedProject');
    return stored ? JSON.parse(stored) : null;
  });

  useEffect(() => {
    console.log('ProjectContext: Selected project updated:', selectedProject);
    if (selectedProject) {
      localStorage.setItem('selectedProject', JSON.stringify(selectedProject));
    } else {
      localStorage.removeItem('selectedProject');
    }
  }, [selectedProject]);

  return (
    <ProjectContext.Provider value={{ selectedProject, setSelectedProject }}>
      {children}
    </ProjectContext.Provider>
  );
};