import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';

const FeatureList = [
  {
    title: 'Testable',
    Svg: require('../../static/img/brobot_logo/brobotStanding2.svg').default, //undraw_docusaurus_mountain
    description: (
      <>
          Image-based process automation is usually developed
          through trial and error. In Brobot, stochastic process flows
          can be mocked and tested.

      </>
    ),
  },
  {
    title: 'State-based',
    Svg: require('../../static/img/brobot_logo/brobot-serious.svg').default, // undraw_docusaurus_tree
    description: (
      <>
        Graphical environments are classified into <code>states</code>. This
          allows for navigation of the environment similar to the way in which
          an html site would be navigated by using its <code>pages</code>.
      </>
    ),
  },
  {
    title: 'Powered by Sikuli & OpenCV',
    Svg: require('../../static/img/brobot_logo/rocket.svg').default, // undraw_docusaurus_react.svg
    description: (
      <>
        Brobot uses Sikuli and OpenCV for automation tasks. Sikuli relies on
          the Java Robot class for controlling
          the mouse and keyboard, and Tesseract for text recognition.
      </>
    ),
  },
];

function Feature({Svg, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} alt={title} />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
