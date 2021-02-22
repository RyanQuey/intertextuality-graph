import React from "react"

import Form from '../../shared/elements/Form';
import Button from '../../shared/elements/Button';
import Select from '../../shared/groups/Select';
import TextReferenceInput from '../../shared/groups/TextReferenceInput';
import HopFieldsSet from "./HopFieldsSet"

import './scss/hop-field-sets-container.scss'

import { connect } from 'react-redux'
import {
  initialChapterOption,
  initialVerseOption,
  bookOptions,
  hopsCountOptions,
  allusionDirectionOptions,
  initialAllusionDirection,
  defaultHopSetParams, 
} from '../../../constants/arc-diagram'

import {
  alertActions,
  formActions,
} from "../../../actions"

import Helpers from '../../../helpers/base-helpers'
import _ from "lodash"

class HopFieldSetsContainer extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
    }

    this.addHop = this.addHop.bind(this)
    this.removeHop = this.removeHop.bind(this)
	}

  componentDidMount () {
    const base = this.props.hopsParams
    if (base == null) {
      const toSet = {
        hopSet0: defaultHopSetParams(),
        hopSet1: defaultHopSetParams(),
      }
      formActions.setParams("HopFieldsSet", "referenceFilter", toSet)
    }
  }

  /**
   *
   * removes selected hop from list, and then renames everything so back to hopSet1 hopSet2 etc in
   * order, with no gaps
   */ 
  removeHop (hopSetIndex) {
	  let newHopsParams = _.clone(this.props.hopsParams)
    // get original keys
    const hopFieldSetsKeys = Object.keys(this.props.hopsParams)

	  // remove key to delete
	  newHopsParams = _.omit(newHopsParams, [`hopSet${hopSetIndex}`]);

    // change indexes of any hop sets that are above the chosen index
    const laterKeys = hopFieldSetsKeys.slice(hopSetIndex + 1)
    laterKeys.forEach(key => {
      const indexForKey = parseInt(key.slice(-1))
      // set it with new key
      newHopsParams[`hopSet${indexForKey - 1}`] = newHopsParams[key]

      // delete the key/value on the original location
      delete newHopsParams[key] 
    })


    const markAsDirty = true
    const overrideOldValues = true
    formActions.setParams("HopFieldsSet", "referenceFilter", newHopsParams, markAsDirty, overrideOldValues)
  }

  addHop (e) {
    e && e.preventDefault && e.preventDefault() 
    
	  const {hopsParams} = this.props
    const hopFieldSetsKeys = Object.keys(hopsParams || {})
    const firstHopKey = hopFieldSetsKeys[0] || -1
    const secondHopKey = hopFieldSetsKeys[1]
    const lastHopIndex = hopFieldSetsKeys.length - 1
    const lastHopKey = hopFieldSetsKeys[hopFieldSetsKeys.length - 1]

    const toSet = {[`hopSet${lastHopIndex + 1}`]: defaultHopSetParams()}

    formActions.setParams("HopFieldsSet", "referenceFilter", toSet)
  }

  render () {
    const { hopsParams } = this.props
    if (!hopsParams) {
      return null
    }

    const hopFieldSetsKeys = Object.keys(hopsParams)
    const hopSetCount = hopFieldSetsKeys.length
    const hopMax = 3
    // add one, since the first hopset is not a hop really
    const canAddHop = hopSetCount < hopMax + 1

    return (
      <div className="hop-field-sets-container">
        <div >
          {hopsParams && Object.keys(hopsParams).map((hopKey, index) => {
            const value = hopsParams[hopKey]

            return (
              <HopFieldsSet 
                index={index}
                key={index}
                removeHop={this.removeHop}
                canRemove={hopSetCount > 2}
              />
            )

          })}
        </div>
        <div>
          <Button
            onClick={this.addHop}
            disabled={!canAddHop}
            small={true}
            rectangle={true}
            type="button"
            title={canAddHop ? "Add a hop to the diagram" : `Cannot have more than ${hopMax} hops`}
          >
            Add Hop
          </Button>
        </div>
      </div>
    )
  }
}

const mapStateToProps = state => {

  return {
    // cannot access props here, so set defaults in a wrapper function (getParams)
    hopsParams: Helpers.safeDataPath(state.forms, "HopFieldsSet.referenceFilter.params", null)
  }
}

export default connect(mapStateToProps)(HopFieldSetsContainer)
