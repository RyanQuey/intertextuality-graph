
## 🚀 Quick start

0. Requirements
 - Make sure to have gatsby cli installed as global lib
    ```shell
    cd gatsby/
    nvm use
    npm install -g gatsby-cli
    ```

1.  **Start developing.**

    Navigate into your new site’s directory and start it up.

    ```shell
    cd gatsby/
    nvm use
    npm i
    # you'll need the gatsby cli too if you don't have it already. Find out how to do that one yourself!
    gatsby develop
    ```

2.  **Open the source code and start editing!**

    Your site is now running at `http://localhost:8080`!

    _Note: You'll also see a second link: _`http://localhost:8080/___graphql`_. This is a tool you can use to experiment with querying your data. Learn more about using this tool in the [Gatsby tutorial](https://www.gatsbyjs.org/tutorial/part-five/#introducing-graphiql)._

    Open the `my-default-starter` directory in your code editor of choice and edit `src/pages/index.js`. Save your changes and the browser will update in real time!

3. Deploy: 


- TODO make this easier by adding a netlify.toml

- Set env vars in Netlify
INTERTEXTUALITY_GRAPH_PLAY_API_URL

## 🧐 What's inside?

A quick look at the top-level files and directories you'll see in a Gatsby project.

    .
    ├── node_modules
    ├── src
    ├── .gitignore
    ├── .prettierrc
    ├── gatsby-browser.js
    ├── gatsby-config.js
    ├── gatsby-node.js
    ├── gatsby-ssr.js
    ├── LICENSE
    ├── package-lock.json
    ├── package.json
    └── README.md

2.  **`/src`**: This directory will contain all of the code related to what you will see on the front-end of your site (what you see in the browser) such as your site header or a page template. `src` is a convention for “source code”.

3.  **`.gitignore`**: This file tells git which files it should not track / not maintain a version history for.

4.  **`.prettierrc`**: This is a configuration file for [Prettier](https://prettier.io/). Prettier is a tool to help keep the formatting of your code consistent.

5.  **`gatsby-browser.js`**: This file is where Gatsby expects to find any usage of the [Gatsby browser APIs](https://www.gatsbyjs.org/docs/browser-apis/) (if any). These allow customization/extension of default Gatsby settings affecting the browser.

6.  **`gatsby-config.js`**: This is the main configuration file for a Gatsby site. This is where you can specify information about your site (metadata) like the site title and description, which Gatsby plugins you’d like to include, etc. (Check out the [config docs](https://www.gatsbyjs.org/docs/gatsby-config/) for more detail).

7.  **`gatsby-node.js`**: This file is where Gatsby expects to find any usage of the [Gatsby Node APIs](https://www.gatsbyjs.org/docs/node-apis/) (if any). These allow customization/extension of default Gatsby settings affecting pieces of the site build process.

8.  **`gatsby-ssr.js`**: This file is where Gatsby expects to find any usage of the [Gatsby server-side rendering APIs](https://www.gatsbyjs.org/docs/ssr-apis/) (if any). These allow customization of default Gatsby settings affecting server-side rendering.


# Why port 8080?
I am developing on cloud9, and by default they only expose 8080 (and maybe 8081 or something). Using hot reload and having all of the data pass back and forth through localserver eats up a lot of bandwidth and uses lots of networking IO, whereas opening the preview app feature in cloud9 means it can all remain in cloud9 besides just what is literally displayed in the browser

# Credits
- Les Mes dataset and visualization based on https://vega.github.io/vega/examples/arc-diagram; all I added was additional interactivity.
- Includes iframes of [STEP Bible](https://github.com/tyndale/step).
