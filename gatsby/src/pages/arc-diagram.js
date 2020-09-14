import React from "react"
import { Link } from "gatsby"

import Layout from "../components/layout"
import Image from "../components/image"
import SEO from "../components/seo"

import { Vega } from 'react-vega';
import spec1 from '../utils/spec1';
import spec2 from '../utils/spec2';
import barData from '../data/sample.json';

// check out their demo for help:
// https://github.com/vega/react-vega/blob/master/packages/react-vega-demo/stories/ReactVegaDemo.tsx


function handleHover(...args){
  console.log(args);
}

const signalListeners = { hover: handleHover };

const ArcDiagram = () => (
  <Layout>
    <SEO title="ArcDiagram" />
      <Vega spec={spec1} data={barData} signalListeners={signalListeners} />,
  </Layout>
)

export default ArcDiagram
