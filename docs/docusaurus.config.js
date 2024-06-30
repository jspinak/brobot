// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

// for KaTeX (Latex)
const math = require('remark-math');
const katex = require('rehype-katex');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Brobot',
  tagline: 'Testable State-based Automation',
  url: 'https://jspinak.github.io',
  baseUrl: '/brobot/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/brobot-happy.ico',
  organizationName: 'jspinak', // Usually your GitHub org/user name.
  projectName: 'brobot', // Usually your repo name.

  presets: [
    [
      '@docusaurus/preset-classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      {
        // Will be passed to @docusaurus/plugin-google-analytics
        gtag: {
          trackingID: 'G-6WY1S6ZWEY',
          anonymizeIP: true,
        },
        pages: {
          remarkPlugins: [math],
          rehypePlugins: [katex],
        },
        docs: {
          path: 'docs',
          remarkPlugins: [math],
          rehypePlugins: [katex],
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl: 'https://jspinak.github.io/brobot/',
        },
        blog: {
          showReadingTime: true,
          editUrl:
            'https://jspinak.github.io/brobot/blog/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
  stylesheets: [
    {
      href: 'https://cdn.jsdelivr.net/npm/katex@0.13.24/dist/katex.min.css',
      type: 'text/css',
      integrity:
          'sha384-odtC+0UGzzFL/6PNoE8rX/SPcQDXBJ+uRepguP4QkPCm2LBxH3FA3y+fKSiJ+AmM',
      crossorigin: 'anonymous',
    },
  ],
  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'Home',
        logo: {
          alt: 'My Site Logo',
          src: 'img/brobot_logo/brobot-happy-text.svg',
        },
        items: [
          {
            type: 'doc',
            docId: 'introduction/introduction',
            position: 'left',
            label: 'Introduction',
          },
          {
            type: 'doc',
            docId: 'tutorial-basics/intro',
            position: 'left',
            label: 'Tutorial',
          },
          {
            type: 'doc',
            docId: 'API/overview',
            position: 'left',
            label: 'Documentation',
          },
          {to: 'visualAPI', label: 'The Visual API', position: 'left'},
          {to: '/blog', label: 'Blog', position: 'left'},
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
          {
            title: 'Docs',
            items: [
              {
              label: 'Introductory Video',
              to: 'https://www.youtube.com/watch?v=aHnVQ8KmOVw',
              },
              {
                label: 'Tutorial',
                to: '/docs/tutorial-basics/intro',
              },
              {
                label: 'The Visual API white paper',
                to: 'visualAPI',
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
              {
                label: 'Twitter',
                href: 'https://twitter.com/brobotJosh',
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
        copyright: `© ${new Date().getFullYear()} Joshua Spinak`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
    }),
};

module.exports = config;
