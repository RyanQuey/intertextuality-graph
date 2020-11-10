/**
 * Implement Gatsby's Browser APIs in this file.
 *
 * See: https://www.gatsbyjs.org/docs/browser-apis/
 */


// needed for using redux
// https://github.com/gatsbyjs/gatsby/blob/master/examples/using-redux/gatsby-browser.js
import wrapWithProvider from "./wrap-with-provider"
export const wrapRootElement = wrapWithProvider

// TODO not working
const _ = require("lodash") 
export const onClientEntry = () => {
  //window._ = lodash
}
//
