// From https://vega.github.io/editor/#/examples/vega/arc-diagram

// I find that passing data in here works best, so everything can be easily ported into the online
// VEGA REPL and also easier to match up what is happening in React with Vega's main api
// TODO this could potentially have memory issues if we repeat the data, so better to not use
// property in data.format, but just only set the data we want on that key.
export default (edgesData, verticesData) => ({

  "$schema": "https://vega.github.io/schema/vega/v5.json",
  // TODO simplifying for now, can add back in later
  // "description": "An arc diagram depicting character co-occurrence in the novel Les Misérables.",
  "width": 770,
  "padding": 5,


  "data": [
    {
      "name": "edges",
      // "url": "data/miserables.json",
      // way to do this, but I'm not sure what it is yet)
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
      /*
       * if wanted to add source name, so don't just show the source id, would do something like:
       * (note that the code below does not work)
      "transform": [
        // {"type": "lookup", from: "nodes", key: "source", fields: ["name"], as: "sourceName", default: ""}
      ],
      */
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
      // "url": "data/miserables.json",
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
    /*
    // data we're storing, to remember state of which vertices are selected
		// https://vega.github.io/vega/examples/interactive-legend/
    {
		// https://vega.github.io/vega/examples/interactive-legend/
      "name": "selectedNodes",
      "on": [
        {"trigger": "clear", "remove": true},
        {"trigger": "clickedNode", "toggle": "clickedNode"}
      ]
    },
    {
      // the same thing, but for edges
      "name": "selectedEdges",
      "on": [
        {"trigger": "clear", "remove": true},
        {"trigger": "clickedEdge", "toggle": "clickedEdge"}
      ]
    }
    */
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
    /*
    {
      "type": "path",
      "from": {"data": "edges"},
      "name": "edgeLabel",
      "encode": {
        /*
        enter: {
          "tooltip": {
            signal: [
              "{title: 'Connection', 'Source Node': '(' + datum.source + ')', 'Target Node': '(' + datum.target + ')'}", 
            ]
          },
        },*/
    /*
        "update": {
          "stroke": {"value": "#000"},
          "strokeOpacity": [
            // if nothing selected, everythign has medium opacity
            //{"test": "!length(data('selectedNodes')) && !length(data('selectedEdges'))", "value": 0.2},
            // if this edge's id is in selected-edges data, or the source or target is selected, make this bolder and everything else lighter
            //{"test": "indata('selectedEdges', 'value', datum.id) || indata('selectedNodes', 'value', datum.source) || indata('selectedNodes', 'value', datum.target) ", "value": 0.3},
            // array values means defaults to last value
            {"value": 0.1},
          ],
          "strokeWidth": {"field": "value"}
        },
        // on hover, increase opacity of strokes for this edge to .6 (make darker)
        "hover": {
          "stroke": {"value": "#000"},
          "strokeOpacity": {"value": 0.3},
          "strokeWidth": {"field": "value"}
        }
      },
      "transform": [
        {
          "type": "lookup", "from": "layout", "key": "datum.index",
          "fields": ["datum.source", "datum.target"],
          "as": ["sourceNode", "targetNode"]
        },
        {
          "type": "linkpath",
          // goes FROM the minimum between the x value of the source and the target
          "sourceX": {"expr": "min(datum.sourceNode.x, datum.targetNode.x)"},
          // goes TO the maximum between the x value of the source and the target
          "targetX": {"expr": "max(datum.sourceNode.x, datum.targetNode.x)"},
          // always ends at 0 (ie, bands go up into an arc then end back at the x axis)
          "sourceY": {"expr": "0"},
          "targetY": {"expr": "0"},
          "shape": "arc"
        }
      ]
    },
    */
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
    // labels/locations for each node
    {
      "type": "text",
      "from": {"data": "nodes"},
      name: "nodeLabel",
      "encode": {
        "update": {
          "x": {"scale": "position", "field": "order"},
          "y": {"value": 7},
          "fontSize": {"value": 9},
          "align": {"value": "right"},
          // if don't set here, will never revert after hovering
          "fontWeight": [
            // make it bolder if selected
            //{"test": "indata('selectedNodes', 'value', datum.index)", "value": 600},
            {"value": 200},
          ],
          "baseline": {"value": "middle"},
          "angle": {"value": -90},
          "text": {"field": "name"}
        },
        // increase font weight for JUST the label when hovering over the label
        "hover": {
          "fontWeight": {"value": 700},
        },
        // how to react when "select" event signal happens on this node
        /*
        "select": {
          "fontWeight": {"value": 900},
        }
        */
      }
    }
  ], // end of marks
	  /*
	"signals": [
	  {
      "name": "clickedNode", "value": null,
      "on": [
        {
          // can use @ sign to refer to a mark (NOTE cannot refer to a data directly I don't think,
          // not for events. Marks get events, data does not)
          "events": "@nodeLabel:click",
					// hopefully adds this node index to the selectedNodes list
          "update": "{value: datum.index}",
          "force":  true
        }
      ]
    },
	  {
      "name": "clickedEdge", "value": null,
      "on": [
        {
          "events": "@edgeLabel:click",
					// hopefully adds this edge id to the selectedEdges list
          "update": "{value: datum.id}",
          "force":  true
        }
      ]
    },
    {
      "name": "clear", "value": true,
      "on": [
        {
          "events": "mouseup[!event.item]",
          "update": "true",
          "force": true
        }
      ]
    },
	],
    */
});
