import { Outlet, Link } from 'react-router-dom';
import { Fragment } from 'react';
import { ReactComponent as BrobotLogo } from '../../assets/brobot-happy.svg';
import Button from '../../components/button/button.component'
import './navigation.styles.scss';

const Navigation = () => {
    return (
        <Fragment>
            <div className='navigation'>
                <Link className='logo-container' to='/'>
                    <BrobotLogo className='logo'/>
                </Link>
                <div className='links-container'>
                    <Link className='nav-link' to='/states'>
                        <Button>STATES</Button>
                    </Link>
                </div>
                <div>
                    <Button>TAKE SCREENSHOTS</Button>
                </div>
                <div>
                    <Button>CREATE STATES FROM SCREENSHOTS</Button>
                </div>
            </div>
            <Outlet />
        </Fragment>
    );
};

export default Navigation;