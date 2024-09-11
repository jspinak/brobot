import React, { useState } from 'react';
import { Button, CircularProgress } from '@mui/material';
import axios from 'axios';

const ButtonComponent = ({ label, endpoint, method = 'POST', onSuccess, onError }) => {
    const [isLoading, setIsLoading] = useState(false);

    const handleClick = async () => {
        setIsLoading(true);
        try {
            const response = await axios[method.toLowerCase()](endpoint);
            onSuccess(response.data);
        } catch (error) {
            onError(error.message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Button
            variant="contained"
            color="primary"
            onClick={handleClick}
            disabled={isLoading}
            sx={{ m: 1 }}
        >
            {isLoading ? <CircularProgress size={24} /> : label}
        </Button>
    );
};

export default ButtonComponent;