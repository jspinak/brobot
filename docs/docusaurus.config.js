// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const {themes} = require('prism-react-renderer');

// for KaTeX (Latex)
const math = require('remark-math').default;
const katex = require('rehype-katex').default;

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Brobot',
  tagline: 'Intelligent Visual Automation',
  url: 'https://jspinak.github.io',
  baseUrl: '/brobot/',
  trailingSlash: false,
  onBrokenLinks: 'warn', // Consider changing to 'throw' for production builds
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/brobot-happy.ico',
  organizationName: 'jspinak', // Usually your GitHub org/user name.
  projectName: 'brobot', // Usually your repo name.

  plugins: [
    [
      '@docusaurus/plugin-google-gtag',
      {
        trackingID: 'G-6WY1S6ZWEY',
        anonymizeIP: true,
      },
    ],
    [
      '@docusaurus/plugin-client-redirects',
      {
        redirects: [
          {
            from: '/docs/tutorial-basics/live-automation',
            to: '/docs/core-library/tutorials/tutorial-basics/live-automation',
          },
          // Version 1.0.6 already has the page at the correct location
          // Version 1.0.7 redirect
          {
            from: '/docs/1.0.7/tutorial-basics/live-automation',
            to: '/docs/1.0.7/core-library/tutorials/tutorial-basics/live-automation',
          },
        ],
      },
    ],
  ],

  presets: [
    [
      '@docusaurus/preset-classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          path: 'docs',
          remarkPlugins: [math],
          rehypePlugins: [katex],
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl: 'https://github.com/jspinak/brobot/edit/main/docs/',
          lastVersion: 'current',
          versions: {
            current: {
              label: 'Latest',
            },
          },
        },
        blog: {
          showReadingTime: true,
          editUrl:
            'https://jspinak.github.io/brobot/blog/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],
  stylesheets: [
    {
      href: 'https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css',
      type: 'text/css',
      integrity:
        'sha384-GvrOXuhMATgEsSwCs4smul74iXGOixntILdUW9XmUC6+HX0sLNAK3q71HotJqlAn',
      crossorigin: 'anonymous',
    },
  ],
  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'Brobot', // Changed from 'Home' for clarity
        logo: {
          alt: 'Brobot Logo',
          src: 'img/brobot_logo/brobot-happy-text.svg',
        },
        items: [
          // A single entry point for all documentation
          {
            type: 'docSidebar',
            sidebarId: 'docSidebar', // Make sure this matches the ID in sidebars.js
            position: 'left',
            label: 'Documentation',
          },
          // The external link to your hosted API documentation
          {
            href: 'https://jspinak.github.io/brobot/api/',
            label: 'API Reference',
            position: 'left',
          },
          {
            label: 'The Visual API',
            to: '/visualAPI',
            position: 'left',
          },
          {to: '/blog', label: 'Blog', position: 'left'},
          // The version dropdown and GitHub link remain on the right
          {
            type: 'docsVersionDropdown',
            position: 'right',
            dropdownActiveClassDisabled: true,
          },
          {
            href: 'https://github.com/jspinak/brobot',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          // Footer links remain the same
          {
            title: 'Docs',
            items: [
              {
                label: 'Introductory Video',
                to: 'https://www.youtube.com/watch?v=aHnVQ8KmOVw',
              },
              {
                label: 'Get Started',
                to: '/docs/getting-started/introduction',
              },
              {
                label: 'The Visual API',
                to: '/visualAPI',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'Stack Overflow',
                href: 'https://stackoverflow.com/questions/tagged/brobot',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'Blog',
                to: '/blog',
              },
              {
                label: 'GitHub',
                href: 'https://github.com/jspinak/brobot',
              },
              {
                label: 'YouTube',
                href: 'https://www.youtube.com/channel/UCgRljq-lHkMLrzRzpJC4HNw',
              },
              {
                label: 'Maven Central',
                href: 'https://search.maven.org/artifact/io.github.jspinak/brobot'
              }
            ],
          },
        ],
        copyright: `© ${new Date().getFullYear()} Joshua Spinak · Free & Open Source`,
      },
      prism: {
        theme: themes.github,
        darkTheme: themes.dracula,
      },
    }),
};

module.exports = config;