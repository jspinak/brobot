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
                to="/docs/getting-started/introduction"
                style={{border: '2px dotted red'}}>
              <div>Get Started</div>
          </Link>
        </div>
        <div>
            <br />Free & open source.
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
      description="Brobot - Testable State-based GUI Automation Framework">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
        <section className={styles.aiSection}>
          <div className="container">
            <div className="row">
              <div className="col col--12 text--center">
                <h2>AI-Assisted Project Creation</h2>
                <p>Let AI help you create your Brobot automation project quickly and easily.</p>
                <Link className="button button--primary button--lg" to="/docs/preparing-folder-for-ai-project-creation">
                  Learn How to Prepare Your Project for AI Assistance
                </Link>
              </div>
            </div>
          </div>
        </section>
      </main>
    </Layout>
  );
}
