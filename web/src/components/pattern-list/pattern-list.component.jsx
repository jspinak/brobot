import React from 'react';
import { Grid } from '@mui/material';
import Pattern from '../pattern/pattern.component';

const PatternList = ({ patterns = [], selectedPatterns, onPatternSelect }) => (
  <Grid container spacing={2}>
    {patterns.map((pattern) => (
      <Grid item key={pattern.id} xs={12} sm={6} md={4} lg={3}>
        <Pattern
          pattern={pattern}
          isSelected={selectedPatterns.includes(pattern.id)}
          onSelect={() => onPatternSelect(pattern.id)}
        />
      </Grid>
    ))}
  </Grid>
);

export default PatternList;