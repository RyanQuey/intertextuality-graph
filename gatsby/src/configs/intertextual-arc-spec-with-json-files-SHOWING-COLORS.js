// From https://vega.github.io/editor/#/examples/vega/arc-diagram

// I find that passing data in here works best, so everything can be easily ported into the online
// VEGA REPL and also easier to match up what is happening in React with Vega's main api
// TODO this could potentially have memory issues if we repeat the data, so better to not use
// property in data.format, but just only set the data we want on that key.
export default (edgesData, verticesData) => ({

  "$schema": "https://vega.github.io/schema/vega/v5.json",
  "width": 770,
  "padding": 5,

  "data": [
    {
      "name": "edges",
      values: edgesData.map((e, index) => {
        // for now, instead of using the vega/d3 api which would have better performance, or even
        // better sending data already formatted from the play api, just
        // getting something working.
        return Object.assign({//(e), {
          id: index,
          source: e.objects[0],
          target: e.objects[1],
          // this was going to set how wide to make the ark
          value: 1,
        })
      }),
      "format": {"type": "json"}, // "property": "links"}
    },
    // aggregates used by nodes transformations to calculate degree
    {
      "name": "sourceDegree",
      "source": "edges",
      "transform": [
        {"type": "aggregate", "groupby": ["source"]}
      ]
    },
    {
      "name": "targetDegree",
      "source": "edges",
      "transform": [
        {"type": "aggregate", "groupby": ["target"]}
      ]
    },
    {
      // aka vertices
      "name": "nodes",
      values: verticesData.map((n, index) => {
        return Object.assign({//(n), {
          index,
          // TODO what to use for this? maybe take random number from 1-25. Better would be to use
          // book order, mapping starting book name to order . Do later
          group: Math.floor(Math.random() * Math.floor(6)),
          name: n.split_passages[0].join(", "),
        })
      }),
      "format": {"type": "json"}, // "property": "nodes"},
      "transform": [
        { "type": "window", "ops": ["rank"], "as": ["order"] },
        // count how many times this node is a source and set as "sourceDegree"
        {
          "type": "lookup", 
          "from": "sourceDegree", 
          "key": "source",
          "fields": ["index"], 
          "as": ["sourceDegree"],
          // start count at 0
          "default": {"count": 0}
        },
        // count how many times this node is a target and set as "targetDegree"
        {
          "type": "lookup", 
          "from": "targetDegree", 
          "key": "target",
          "fields": ["index"], 
          "as": ["targetDegree"],
          "default": {"count": 0}
        },
        {
          "type": "formula", "as": "degree",
          "expr": "datum.sourceDegree.count + datum.targetDegree.count"
        }
      ]
    },
  ],

  "scales": [
    {
      "name": "position",
      "type": "band",
      "domain": {"data": "nodes", "field": "order", "sort": true},
      "range": "width"
    },
    {
      "name": "color",
      "type": "ordinal",
      "range": "category",
      "domain": {"data": "nodes", "field": "group"}
    }
  ],

  "marks": [
    // these are the little dots at the ends of the labels, where the edges meet 
    // OR maybe this is all invisible, but makes placeholders that other marks refer to (hence,
    // opacity 0)
    {
      "type": "symbol",
      "name": "layout",
      "interactive": false,
      "from": {"data": "nodes"},
      "encode": {
        "enter": {
          "opacity": {"value": 0}
        },
        "update": {
          "x": {"scale": "position", "field": "order"},
          "y": {"value": 0},
          "size": {"field": "degree", "mult": 5, "offset": 10},
          "fill": {"scale": "color", "field": "group"}
        },
      }
    },
    // the bands for each edge connecting nodes
    // I think styles the colored dot for each node further
    {
      "type": "symbol",
      "from": {"data": "layout"},
      "encode": {
        "update": {
          "x": {"field": "x"},
          "y": {"field": "y"},
          "fill": {"field": "fill"},
          "size": {"field": "size"}
        }
      }
    },
  ], // end of marks
});
