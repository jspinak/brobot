import React, { useState, useEffect } from 'react';
import { FormControl, InputLabel, Select, MenuItem } from '@mui/material';

const ProjectSelector = ({ onProjectChange, triggerRefresh }) => {
  const [projects, setProjects] = useState([]);
  const [selectedProject, setSelectedProject] = useState('');

  useEffect(() => {
    // Fetch projects from your API
    const fetchProjects = async () => {
      try {
        const response = await fetch(`${process.env.REACT_APP_BROBOT_API_URL}/api/projects`);
        if (!response.ok) {
          const text = await response.text();
          console.error('Error response:', text);
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        setProjects(data);
      } catch (error) {
        console.error('Error fetching projects:', error);
      }
    };

    fetchProjects();
  }, [triggerRefresh]);

const handleChange = (event) => {
  const projectId = event.target.value;
  console.log('Selected project ID:', projectId);

  const selectedProject = projects.find(p => p.id === projectId);
  console.log('Selected project:', selectedProject);

  if (selectedProject) {
    setSelectedProject(selectedProject);
    if (onProjectChange) {
      onProjectChange(selectedProject.id, selectedProject.name);
    }
  } else {
    console.error('Selected project not found');
    setSelectedProject(null);
    if (onProjectChange) {
      onProjectChange(null, '');
    }
  }
};

  return (
    <FormControl fullWidth>
      <InputLabel id="project-select-label">Project</InputLabel>
        <Select
          labelId="project-select-label"
          id="project-select"
          value={selectedProject ? selectedProject.id : ''}
          label="Project"
          onChange={handleChange}
        >
          <MenuItem value="">
            <em>None</em>
          </MenuItem>
          {projects.map((project) => (
            <MenuItem key={project.id} value={project.id}>
              {project.name}
            </MenuItem>
          ))}
        </Select>
    </FormControl>
  );
};

export default ProjectSelector;