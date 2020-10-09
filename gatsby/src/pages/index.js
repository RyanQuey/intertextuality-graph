import React from "react"
import { Link } from "gatsby"

import Layout from "../components/layout"
import Image from "../components/image"
import SEO from "../components/seo"

const IndexPage = () => (
  <Layout>
    <SEO title="Home" />
    <Link to="/block-diagram/">Go to "Sample Block Diagram"</Link>
    <br />
    <Link to="/arc-diagram/">Go to "Sample Arc Diagram (Les Mis)"</Link>
    <br />
    <Link to="/intertextuality-arc-diagram/">Go to "Intertextuality Arc Diagram"</Link>
  </Layout>
)

export default IndexPage
