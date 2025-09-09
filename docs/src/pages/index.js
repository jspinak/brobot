import React, { useEffect } from 'react';
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
        <div className={styles.heroContent}>
          <div className={styles.heroText}>
            <div className={styles.heroHeader}>
              <img src="img/brobot_logo/brobot-happy-text.svg" alt="Brobot Icon" className={styles.heroIcon} />
              <h1 className={clsx('hero__title', styles.heroTitle)}>{siteConfig.title}</h1>
            </div>
            <p className={clsx('hero__subtitle', styles.heroSubtitle)}>{siteConfig.tagline}</p>
            <div className={styles.aiBox}>
              <h3>AI-Assisted Project Creation</h3>
              <p>Let AI help you create your Brobot automation project quickly and easily.</p>
              <Link className={clsx("button button--primary button--sm", styles.aiButton)} to="/docs/getting-started/preparing-folder-for-ai-project-creation">
                <span className={styles.aiButtonText}>
                  <span className={styles.desktopText}>Learn How to Prepare Your Project for AI Assistance</span>
                  <span className={styles.mobileText}>Learn How to Prepare Your<br />Project for AI Assistance</span>
                </span>
              </Link>
            </div>
          </div>
          <div className={styles.heroVideos}>
            <div className={styles.videoItem}>
              <h3 className={styles.videoTitle}>Introduction to Brobot</h3>
              <div className={styles.videoWrapper}>
                <div style={{padding:"56.25% 0 0 0", position:"relative"}}>
                  <iframe 
                    src="https://player.vimeo.com/video/1104575435?badge=0&amp;autopause=0&amp;player_id=0&amp;app_id=58479" 
                    frameBorder="0" 
                    allow="autoplay; fullscreen; picture-in-picture; clipboard-write; encrypted-media; web-share" 
                    referrerPolicy="strict-origin-when-cross-origin" 
                    style={{position:"absolute", top:0, left:0, width:"100%", height:"100%"}} 
                    title="brobot-in-city"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}

export default function Home() {
  const {siteConfig} = useDocusaurusContext();
  
  useEffect(() => {
    const script = document.createElement('script');
    script.src = 'https://player.vimeo.com/api/player.js';
    script.async = true;
    document.head.appendChild(script);
    
    return () => {
      document.head.removeChild(script);
    };
  }, []);
  
  return (
    <Layout
      title={`Hello from ${siteConfig.title}`}
      description="Brobot - Testable State-based GUI Automation Framework">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
        <section className={styles.videoSection}>
          <div className="container">
            <div className={styles.videosContainer}>
              <div className={styles.videoColumn}>
                <h3 className={styles.videoTitle}>Solving Automation Complexity with Brobot</h3>
                <div className={styles.videoWrapper}>
                  <div style={{padding:"56.25% 0 0 0", position:"relative"}}>
                    <iframe 
                      src="https://www.youtube.com/embed/aHnVQ8KmOVw?si=TyaApImJe6bhCrL9" 
                      title="YouTube video player" 
                      frameBorder="0" 
                      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" 
                      referrerPolicy="strict-origin-when-cross-origin" 
                      allowFullScreen
                      style={{position:"absolute", top:0, left:0, width:"100%", height:"100%"}}
                    />
                  </div>
                </div>
              </div>
              <div className={styles.videoColumn}>
                <h3 className={styles.videoTitle}>Demo: Creating a Labeled Image Dataset</h3>
                <div className={styles.videoWrapper}>
                  <div style={{padding:"56.25% 0 0 0", position:"relative"}}>
                    <iframe 
                      src="https://player.vimeo.com/video/1104905215?badge=0&amp;autopause=0&amp;player_id=0&amp;app_id=58479" 
                      frameBorder="0" 
                      allow="autoplay; fullscreen; picture-in-picture; clipboard-write; encrypted-media; web-share" 
                      referrerPolicy="strict-origin-when-cross-origin" 
                      style={{position:"absolute", top:0, left:0, width:"100%", height:"100%"}} 
                      title="Automation Demo - Creating a Labeled Image Dataset"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </Layout>
  );
}
