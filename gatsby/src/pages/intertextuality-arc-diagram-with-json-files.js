import React from "react"
import { Link } from "gatsby"

import Layout from "../components/layout"
import Image from "../components/image"
import SEO from "../components/seo"

import { Vega } from 'react-vega';
import spec1 from '../configs/intertextual-arc-spec-with-json-files';
import { Handler } from 'vega-tooltip';

import edgesData from '../data/intertextuality-edges.json';
import verticesData from '../data/intertextuality-vertices.json';


/*
 * Just a test component to see what kind of data I'm getting from my API, and how to manipulate it
 *
 */ 

function handleHover(...args){
  console.log(args);
}

const signalListeners = { hover: handleHover };
const tooltipOptions = {
  theme: "dark"
}

const tooltip = new Handler(tooltipOptions).call


const spec = spec1(edgesData, verticesData)
console.log(spec)

const IArcDiagramWithFile = () => (
  <Layout>
    <SEO title="IArcDiagramWithFile" />

    <Vega 
      spec={spec} 
    />
  </Layout>
)

export default IArcDiagramWithFile
