import React from "react"
import { Link } from "gatsby"

import Layout from "../components/layout"
import Image from "../components/image"
import SEO from "../components/seo"

import { Vega } from 'react-vega';
import spec1 from '../configs/arc-spec1';
// add a tooltip https://stackoverflow.com/a/53709494/6952495
// https://github.com/vega/vega-tooltip/blob/master/docs/customizing_your_tooltip.md
// examples (can look at source code in Vega Editor)
// https://vega.github.io/vega-tooltip/vega-examples.html
import { Handler } from 'vega-tooltip';


// I'm using Approach #2 from vega docs
// https://github.com/vega/react-vega/tree/master/packages/react-vega#approach2-use-vega-generic-class-and-pass-in-spec-for-dynamic-component
// want vega for more power, but want flexibility of react. Though...maybe later it's 

// https://vega.github.io/editor/data/miserables.json
// if put "url" instead "of" in the data in specification, can load a json or CSV file
import arcData from '../data/les-mis-arc-diagram-sample.json';

// check out their demo for help:
// https://github.com/vega/react-vega/blob/master/packages/react-vega-demo/stories/ReactVegaDemo.tsx


const spec = spec1(arcData)

console.log("sample arc diagram spec", Object.assign({}, spec))

const ArcDiagram = () => {
  function handleHover(...args){
    console.log(args);
  }

  // TODO check to see if this does anything
  const signalListeners = { hover: handleHover };
  const tooltipOptions = {
    theme: "dark"
  }

  let tooltip
  function setTooltip (ready) {
    tooltip = ready ? new Handler(tooltipOptions).call : {}
  }

  React.useEffect(() => {
    setTooltip(true);
  }, []);
  

  return (
  <Layout>
    <SEO title="ArcDiagram" />

    <Vega 
      spec={spec} 
      signalListeners={signalListeners} 
      tooltip={tooltip}
    />
  </Layout>
  )
}

export default ArcDiagram
