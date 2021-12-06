import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';

const FeatureList = [
  {
    title: 'Testable',
    Svg: require('../../static/img/undraw_docusaurus_mountain.svg').default,
    description: (
      <>
          Image-based process automation is usually developed
          through trial and error. In brobot, stochastic process flows
          can be mocked and tested.

      </>
    ),
  },
  {
    title: 'State-based',
    Svg: require('../../static/img/undraw_docusaurus_tree.svg').default,
    description: (
      <>
        Graphical environments are classified into <code>states</code>. This
          allows for navigation of the environment similar to the way in which
          an html site would be navigated by using its <code>pages</code>.
      </>
    ),
  },
  {
    title: 'Powered by Sikuli',
    Svg: require('../../static/img/undraw_docusaurus_react.svg').default,
    description: (
      <>
        brobot uses Sikuli to interact with the GUI.
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
