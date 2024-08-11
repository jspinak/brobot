import { Outlet, Link } from 'react-router-dom';
import { Fragment } from 'react';
import brobotHappy from '../../components/assets/brobot-happy.svg';
import Button from '../../components/button/button.component'
import './navigation.styles.scss';

const NavigationBar = () => {
  return (
    <nav>
      <ul>
        <li>
          <Link to="/">
            <img src={brobotHappy} alt="Home" />
          </Link>
        </li>
        <li>
          <Link to="/states">States</Link>
        </li>
        <li>
          <Link to="/states/1">State Details</Link>
        </li>
        <li>
          <Link to="/create-state">Create State</Link>
        </li>
      </ul>
    </nav>
  );
};

export default NavigationBar;