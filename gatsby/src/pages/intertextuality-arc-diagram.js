import React from "react"
import { Link } from "gatsby"

import Layout from "../components/layout"
import Image from "../components/image"
import SEO from "../components/seo"

import { Vega } from 'react-vega';
// TODO for some reason changing this file does not really get updated by webpack hot reload
import specBuilder from '../configs/intertextual-arc-spec';
import { Handler } from 'vega-tooltip';

import edgesData from '../data/intertextuality-edges.json';
import targetVerticesData from '../data/intertextuality-vertices.json';
import sourceVerticesData from '../data/intertextuality-source-vertices.json';
import allVerticesData from '../data/intertextuality-all-vertices.json';


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
const apiUrl = "http://localhost:9000"

// const sourceVerticesPath = apiUrl + "/sources-for-ref"
// const targetVerticesPath = apiUrl + "/texts-starting-with-ref"
// const allVerticesData = sourceVerticesData.concat(targetVerticesData)

const verticesUrl = apiUrl + "/sources-for-ref-with-alluding-texts"
const edgesUrl = apiUrl + "/paths-for-sources-starting-with-ref"

// merge vertices data together

class IArcDiagram extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      spec: specBuilder(edgesData, allVerticesData, edgesUrl, verticesUrl) 
    }
    console.log("intertextuality graph", this.state.spec)
  }

  buildSpec () {
    return edgesUrl
  }

  render () {
    return (
      <Layout>
        <SEO title="Intertextuality Arc Diagram" />

        <Vega 
          spec={this.state.spec} 
        />
      </Layout>
    )
  }
}

export default IArcDiagram
