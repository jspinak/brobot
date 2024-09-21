import React, { useCallback, useEffect, useState } from 'react';
import ReactFlow, {
  MiniMap,
  Controls,
  Background,
  useNodesState,
  useEdgesState,
  addEdge,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { CircularProgress, Typography } from '@mui/material';
import api from './../../services/api'

const TransitionGraph = ({ states, isLoading, selectedProject }) => {
  const [graphData, setGraphData] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchGraphData = async () => {
      if (!selectedProject) {
        console.log('No project selected');
        return;
      }

      try {
        console.log(`Fetching graph data for project ID: ${selectedProject.id}`);
        const response = await api.get(`/api/transitions/graph-data/${selectedProject.id}`);
        console.log('API Response:', response);
        console.log('Received graph data:', response.data);

        if (response.data && response.data.states && response.data.transitions) {
          console.log('Valid graph data structure received');
          setGraphData(response.data);
          setError(null);
        } else {
          console.error('Invalid data structure received from API:', response.data);
          throw new Error('Invalid data structure received from API');
        }
      } catch (err) {
        console.error('Error fetching graph data:', err);
        setError(`Failed to fetch graph data: ${err.message}`);
        setGraphData(null);
      }
    };

    fetchGraphData();
  }, [selectedProject]);

  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);

  useEffect(() => {
    if (graphData) {
      const newNodes = graphData.states.map((state) => ({
        id: state.id.toString(),
        data: { label: state.name },
        position: { x: Math.random() * 500, y: Math.random() * 500 },
      }));

      const newEdges = graphData.transitions.map((transition) => ({
        id: `e${transition.id}`,
        source: transition.sourceStateId.toString(),
        target: transition.statesToEnter && transition.statesToEnter.length > 0
          ? transition.statesToEnter[0].toString()
          : transition.sourceStateId.toString(),
        animated: true,
        label: `Score: ${transition.score}`,
      }));

      setNodes(newNodes);
      setEdges(newEdges);
    }
  }, [graphData]);

  const onConnect = useCallback(
    (params) => setEdges((eds) => addEdge(params, eds)),
    [setEdges]
  );

  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Typography color="error">{error}</Typography>;
  }

  if (!graphData) {
    return <Typography>No graph data available. Please select a project.</Typography>;
  }

  return (
    <div style={{ width: '100%', height: '80vh' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        fitView
      >
        <Controls />
        <MiniMap />
        <Background variant="dots" gap={12} size={1} />
      </ReactFlow>
    </div>
  );
};

export default TransitionGraph;