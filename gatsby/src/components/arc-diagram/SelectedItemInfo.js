import React from "react"

import './scss/selected-item-info.scss'


class SelectedItemInfo extends React.Component {
  constructor (props) {
    super(props)

    this.state = {

    }
  }

  componentDidMount () {
  }

  render () {
    const { selectedNode, selectedEdge  } = this.props

    return (
      <div id="selected-item-info">
        {selectedEdge && (
          <div className="selected-item-container">
            <h2 className="selected-item-header">
              Selected Connection
            </h2>
            <div className="selected-item-fields">
              <div className="selected-item-field-container">
                <div>Source Text:</div>
                <div>{selectedEdge.edgeData.sourceSplitPassages[0]}</div>
              </div>
              <div className="selected-item-field-container">
                <div>Alluding Text:</div>
                <div>{selectedEdge.edgeData.alludingSplitPassages[0]}</div>
              </div>
              <div className="selected-item-field-container">
                <div>Connection type:</div>
                <div>{selectedEdge.edgeData.connectionType}</div>
              </div>
              <div className="selected-item-field-container">
                <div>Confidence level:</div>
                <div>{selectedEdge.edgeData.confidenceLevel}</div>
              </div>
              <div className="selected-item-field-container">
                <div>Volume level:</div>
                <div>{selectedEdge.edgeData.volumeLevel}</div>
              </div>
              <div className="selected-item-field-container">
                <div>Description:</div>
                <div>{selectedEdge.edgeData.description}</div>
              </div>
              <div className="selected-item-field-container">
                <div>Source version:</div>
                <div>{selectedEdge.edgeData.sourceVersion}</div>
              </div>
              <div className="selected-item-field-container">
                <div>Beale Categories:</div>
                <div>{selectedEdge.edgeData.bealeCategories}</div>
              </div>
              <div className="selected-item-field-container">
                <div>Comments:</div>
                <div>{selectedEdge.edgeData.comments}</div>
              </div>
            </div>
          </div>
        )}
        {selectedNode && (
          <div className="selected-item-container">
            <h2 className="selected-item-header">
              Selected Text
            </h2>
            <div className="selected-item-fields">
              <div className="selected-item-field-container">
                <div>Passage:</div>
                <div> {selectedNode.nodeData.split_passages[0]}</div>
              </div>
              <div className="selected-item-field-container">
                <div>Is source for how many passages here:</div>
                <div> {selectedNode.nodeData.sourceDegree.count}</div>
              </div>
              <div className="selected-item-field-container">
                <div>Alludes to how many passages here:</div>
                <div> {selectedNode.nodeData.alludingDegree.count}</div>
              </div>
              <div className="selected-item-field-container">
                <div>Comments:</div>
                <div>{selectedNode.nodeData.comments}</div>
              </div>
              <div className="selected-item-field-container">
                <div>Description:</div>
                <div>{selectedNode.nodeData.description}</div>
              </div>
            </div>
          </div>
        )}
      </div>

    )
  }
}

export default SelectedItemInfo
