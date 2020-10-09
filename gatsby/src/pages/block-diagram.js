import React from "react"
import { Link } from "gatsby"

import Layout from "../components/layout"
import Image from "../components/image"
import SEO from "../components/seo"

import { Vega } from 'react-vega';
import spec1 from '../configs/block-spec1';

// I'm using Approach #2 from vega docs
// https://github.com/vega/react-vega/tree/master/packages/react-vega#approach2-use-vega-generic-class-and-pass-in-spec-for-dynamic-component
// want vega for more power, but want flexibility of react. Though...maybe later it's 

// https://github.com/vega/react-vega/tree/master/packages/react-vega#data
// if put "url" instead "of" in the data in specification, can load a json or CSV file
import barData from '../data/block-diagram-sample.json';

// check out their demo for help:
// https://github.com/vega/react-vega/blob/master/packages/react-vega-demo/stories/ReactVegaDemo.tsx


function handleTooltip(...args){
  console.log("tooltip", args);
}

// NOTE keys correspond to the signals defined in the spec
const signalListeners = { tooltip: handleTooltip };

const BlockDiagram = () => (
  
  <Layout>
    <SEO title="Block Diagram" />

		<Vega spec={spec1} data={barData} signalListeners={signalListeners} />
  </Layout>
)

export default BlockDiagram
