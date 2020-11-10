/**
 * Implement Gatsby's SSR (Server Side Rendering) APIs in this file.
 *
 * See: https://www.gatsbyjs.org/docs/ssr-apis/
 */

// https://www.gatsbyjs.com/docs/adding-redux-store/
import wrapWithProvider from "./wrap-with-provider"
export const wrapRootElement = wrapWithProvider
