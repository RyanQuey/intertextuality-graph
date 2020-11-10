import React from "react"
import Form from '../shared/elements/Form';
import Button from '../shared/elements/Button';
import Select from '../shared/groups/Select';

import classes from './scss/diagram-options-form.scss'
import HopFieldsSet from "./HopFieldsSet"
import _ from "lodash"
import { connect } from 'react-redux'

import {
  bookOptions,
  hopsCountOptions,
  dataSetOptions,
  allusionDirectionOptions,
} from '../../constants/arc-diagram'



class DiagramOptionsForm extends React.Component {
  constructor (props) {
    super(props)




    this.state = {

    }
    
    this.getHopsCountOptions = this.getHopsCountOptions.bind(this)

	}

  componentDidMount () {
  }
  
  getHopsCountOptions () {
    const { filterByChapter, filterByVerse } = this.props
    if (!filterByChapter) {
      // if filter by book, max out at 1
      return hopsCountOptions.slice(0, 1)
    } else if (!filterByVerse) {
      // if filter by chpater, max out at 2
      return hopsCountOptions.slice(0, 2)
      
    } else {
      // as much as the options constant allows!
      return hopsCountOptions 
    }
  }

  setFilterBy (e, value) {
    e.preventDefault && e.preventDefault()

    const newState = {}
    const paramOverrides = {}

    if (value === "verse") {
      if (!value && this.state.hopsCount.value > 2) {
        newState.hopsCount = hopsCountOptions[1] 
        paramOverrides.hopsCount = 2 
      }
      
    } else if (value === "chapter") {
      if (!value && this.state.hopsCount.value > 1) {
        newState.hopsCount = hopsCountOptions[0] 
        paramOverrides.hopsCount = 1 
        console.log("setting as", newState)
      }

    } else if (value === "book") {
    }

    // max of 0 hops count when filtering by book
    this.setState(newState)
    this.refreshData(paramOverrides)
  }

  render () {
    const { startingBook, startingChapter, startingVerse, allusionDirection, dataSet, hopsCount, filterByChapter, filterByVerse } = this.props

    console.log("now filtering by chapter?", filterByChapter)

    return (
      <div className={"configForm"}>
        <Form>
          <HopFieldsSet 
            allusionDirection={allusionDirection} 
            filterByChapter={filterByChapter} 
            filterByVerse={filterByVerse}  
            selectStartingBook={this.props.selectStartingBook}
            selectStartingChapter={this.props.selectStartingChapter}
            selectStartingVerse={this.props.selectStartingVerse}
            // simply hard coding for now
            index={0}
          />
          <div className="other-configs">
            <div>
              Hops:
              <Select 
                options={this.getHopsCountOptions()}
                onChange={this.props.changeHopsCount}
                currentOption={hopsCount}
              />
            </div>
            <div>
              Data Set
              <Select 
                options={dataSetOptions}
                onChange={this.props.selectDataSet}
                currentOption={dataSet}
              />
            </div>
          </div>
        </Form>

      </div>
    )
  }
}

const mapStateToProps = state => {
  return {
    alerts: _.values(state.alerts) || [],
  }
}

export default connect(mapStateToProps)(DiagramOptionsForm)
