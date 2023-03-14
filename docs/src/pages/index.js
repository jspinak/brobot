import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import HomepageFeatures from '../components/HomepageFeatures';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <h1 className="hero__title">{siteConfig.title}</h1>
        <p className="hero__subtitle">{siteConfig.tagline}</p>
        <div>
          <Link className="button button--secondary button--lg" to="https://www.youtube.com/watch?v=aHnVQ8KmOVw"
                style={{border: '2px dotted red'}}>
              <div>Introductory Video</div>
          </Link>
        </div>
        <div className={styles.buttons}>
          <Link className="button button--secondary button--lg"
                to="../brobot/docs/tutorial-state-structure-builder/intro"
                style={{border: '2px dotted red'}}>
              <div>Get Started</div>
          </Link>
        </div>
        <div>
            <br />Free & open source. The latest version is 1.0.6.
        </div>
      </div>
    </header>
  );
}

export default function Home() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`Hello from ${siteConfig.title}`}
      description="Description will go into a meta tag in <head />">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
