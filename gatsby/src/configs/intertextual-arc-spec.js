// From https://vega.github.io/editor/#/examples/vega/arc-diagram

// I find that passing data in here works best, so everything can be easily ported into the online
// VEGA REPL and also easier to match up what is happening in React with Vega's main api
// TODO this could potentially have memory issues if we repeat the data, so better to not use
// property in data.format, but just only set the data we want on that key.
export default (edgesUrl, verticesUrl) => ({

  "$schema": "https://vega.github.io/schema/vega/v5.json",
  // TODO simplifying for now, can add back in later
  // "description": "An arc diagram depicting character co-occurrence in the novel Les Misérables.",
  "width": 770,
  "padding": 5,


  "data": [
    {
      "name": "edges",
      // way to do this, but I'm not sure what it is yet)
      url: edgesUrl,
      "format": {"type": "json"}, // "property": "links"}
      "transform": [
        {
          "type": "project", 
          fields: ["objects[0].id[0]", "objects[0].split_passages[0]", "objects[1].id[0]", "objects[1].split_passages[0]"], 
          as: ["sourceId", "sourceSplitPassages", "targetId", "targetSplitPassages"], 
        },
        {"type": "identifier", "as": "id"},
      ],
    },
    // aggregates used by nodes transformations to calculate degree
    {
      "name": "sourceDegree",
      "source": "edges",
      "transform": [
        {"type": "aggregate", "groupby": ["sourceId"]}
      ]
    },
    {
      "name": "targetDegree",
      "source": "edges",
      "transform": [
        {"type": "aggregate", "groupby": ["targetId"]}
      ]
    },
    {
      // aka vertices
      "name": "nodes",
      url: verticesUrl,
      "format": {"type": "json"}, // "property": "nodes"},
      "transform": [
        // grab only certain fields, and drill down as we do so
        {
          "type": "project", 
          fields: ["id[0]", "split_passages[0]"], 
          as: ["id", "split_passages"], 
        },
        // convert fields using an expression
        { "type": "formula", "expr": "join(datum.split_passages, ', ')", as: "passages" },
        { "type": "window", "ops": ["rank"], "as": ["order"] },
        // count how many times this node is a source and set as "sourceDegree"
        {
          "type": "lookup", 
          "from": "sourceDegree", 
          "key": "sourceId",
          "fields": ["id"], 
          "as": ["sourceDegree"],
          // start count at 0
          "default": {"count": 0}
        },
        // count how many times this node is a target and set as "targetDegree"
        {
          "type": "lookup", 
          "from": "targetDegree", 
          // foreign key
          "key": "targetId",
          // primary key of the node (?)
          "fields": ["id"], 
          "as": ["targetDegree"],
          "default": {"count": 0}
        },
        {
          "type": "formula", "as": "degree",
          "expr": "datum.sourceDegree.count + datum.targetDegree.count"
        }
      ]
    },
    // data we're storing, to remember state of which vertices are selected
    {
		// https://vega.github.io/vega/examples/interactive-legend/
      "name": "selectedNodes",
      "on": [
        {"trigger": "clear", "remove": true},
        {"trigger": "clickedNode", "toggle": "clickedNode"},
      ]
    },
    {
      // the same thing, but for edges
      "name": "selectedEdges",
      "on": [
        {"trigger": "clear", "remove": true},
        {"trigger": "clickedEdge", "toggle": "clickedEdge"},
      ]
    }
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
      "domain": {"data": "nodes", "field": "order"}
    }
  ],

  "marks": [
    // I think maps out x/y coordinates for our nodes
    // I think all invisible, but makes placeholders that other marks refer to (hence,
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
          "fill": {"scale": "color", "field": "order"}
        },
      }
    },
    // I think styles the colored dot for each node 
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
            {"test": "indata('selectedNodes', 'value', datum.id)", "value": 600},
            // make it bolder if edge is selected and this node's id is the sourceId
            {"test": "indata('selectedEdges', 'sourceId', datum.id)", "value": 600},
            // make it bolder if edge is selected and this node's id is the targetId
            {"test": "indata('selectedEdges', 'targetId', datum.id)", "value": 600},
            {"value": 200},
          ],
          "baseline": {"value": "middle"},
          "angle": {"value": -90},
          "text": {"field": "passages"}
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
    },
    // the bands for each edge connecting nodes
    {
      "type": "path",
      "from": {"data": "edges"},
      "name": "edgeLabel",
      "encode": {
        enter: {
          "tooltip": {
            signal: [
              "{title: 'Connection', 'Source Node': '- ' + datum.sourceSplitPassages, 'Target Node': '- ' + datum.targetSplitPassages}", 
            ]
          },
        },
        "update": {
          "stroke": {"value": "#000"},
          "strokeOpacity": [
            // if nothing selected, everythign has medium opacity
            {"test": "!length(data('selectedNodes')) && !length(data('selectedEdges'))", "value": 0.2},
            // if this edge's id is in selected-edges data, or the source or target is selected, make this bolder and everything else lighter
            {"test": "indata('selectedEdges', 'value', datum.id) || indata('selectedNodes', 'value', datum.sourceId) || indata('selectedNodes', 'value', datum.targetId) ", "value": 0.3},
            // array values means defaults to last value
            {"value": 0.1},
          ],
          "strokeWidth": {"value": 1, "mult": 3},
        },
        // on hover, increase opacity of strokes for this edge to .6 (make darker)
        "hover": {
          "stroke": {"value": "#000"},
          "strokeOpacity": {"value": 0.3},
          "strokeWidth": {"value": 2, "mult": 5},
        }
      },
      "transform": [
        {
          "type": "lookup", "from": "layout", 
          // each layout datum takes its fields from nodes, so has an id that corresponds to a node,
          // with the same id as that node
          "key": "datum.id",
          // take source (which is source id) and target (which is target id) from this edge and map
          // them to the id on the layout to set sourceNode and targetNode here on the edge band's
          // fields
          "fields": ["datum.sourceId", "datum.targetId"],
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
  ], // end of marks
	"signals": [
	  {
      "name": "clickedNode", "value": null,
      "on": [
        {
          // can use @ sign to refer to a mark (NOTE cannot refer to a data directly I don't think,
          // not for events. Marks get events, data does not)
          "events": "@nodeLabel:click",
					// hopefully adds this node id to the selectedNodes list
          "update": "{value: datum.id}",
          "force":  true
        }
      ]
    },
	  {
      "name": "clickedEdge", "value": null,
      // what events will trigger this signal
      "on": [
        {
					// when an edgeLabel is clicked, adds this edge id to whatever listens to this signal (since the edgeLabel has id that corresponds to edge)
          "events": "@edgeLabel:click",
					// corresponds to the edge)
          "update": "{value: datum.id, sourceId: datum.sourceId, targetId: datum.targetId}",
          "force":  true
        },
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
});
