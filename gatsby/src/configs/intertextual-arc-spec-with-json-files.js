// From https://vega.github.io/editor/#/examples/vega/arc-diagram

// I find that passing data in here works best, so everything can be easily ported into the online
// VEGA REPL and also easier to match up what is happening in React with Vega's main api
// TODO this could potentially have memory issues if we repeat the data, so better to not use
// property in data.format, but just only set the data we want on that key.
export default (edgesData, verticesData) => ({

  "$schema": "https://vega.github.io/schema/vega/v5.json",
  // TODO simplifying for now, can add back in later
  "width": 770,
  "padding": 5,


  "data": [
    // aggregates used by nodes transformations to calculate degree
    {
      // aka vertices
      "name": "nodes",
      values: verticesData.map((v, index) => {
        return Object.assign((v), {index})
      }),
      "format": {"type": "json"}, // "property": "nodes"},
    },
    {
      "name": "edges",
      values: edgesData.map((e, index) => {
        return Object.assign((e), {index})
      }),
      "format": {"type": "json"}, // "property": "nodes"},
      /*
      "transform": [
				{
					"type": "fold",
					"fields": ["datum.objects[0]"],
					"as": ["source"],
				},
				{
					"type": "fold",
					"fields": ["objects[1]"],
					"as": ["target"],
				},
			]
      */
    },
  ],

  "scales": [
    {
      "name": "position",
      "type": "band",
      "domain": {"data": "nodes", "field": "index", "sort": true},
      "range": "width"
    },
    {
      "name": "color",
      "type": "ordinal",
      "range": "category",
      "domain": {"data": "nodes", "field": "index"}
    }
  ],

  "marks": [
    {
      "type": "text",
      "from": {"data": "nodes"},
      name: "nodeLabel",
      "encode": {
        "update": {
          "x": {"scale": "position", "field": "index"},
          "y": {"value": 7},
          "fontSize": {"value": 9},
          "align": {"value": "right"},
          // if don't set here, will never revert after hovering
          "fontWeight": [
            {"value": 400},
          ],
          "baseline": {"value": "middle"},
          "angle": {"value": -90},
          //"text": {"field": "datum.starting_book[0]"}
          "text": {"field": "datum.index"}
        },
      }
    },
    // I made this one from scratch, just trying to get something on the screen
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
          "x": {"scale": "position", "field": "index"},
          "y": {"value": 0},
          "size": {"value": 12},
        },
      },
    },
  ]
});
