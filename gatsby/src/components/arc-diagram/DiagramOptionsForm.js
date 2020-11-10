import React from "react"
import Form from '../shared/elements/Form';
import Button from '../shared/elements/Button';
import Select from '../shared/groups/Select';

import classes from './scss/diagram-options-form.scss'
import HopFieldsSet from "./HopFieldsSet"

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

  render () {
    const { startingBook, startingChapter, startingVerse, chapterOptions, verseOptions, allusionDirection, dataSet, hopsCount, filterByChapter, filterByVerse } = this.props

    console.log("now filtering by chapter?", filterByChapter)


    return (
      <div className={"configForm"}>
        <Form>
          <HopFieldsSet 
            startingBook={startingBook} 
            startingChapter={startingChapter} 
            startingVerse={startingVerse} 
            chapterOptions={chapterOptions} 
            verseOptions={verseOptions} 
            allusionDirection={allusionDirection} 
            dataSet={dataSet} 
            hopsCount={hopsCount} 
            filterByChapter={filterByChapter} 
            filterByVerse={filterByVerse}  
            selectStartingBook={this.props.selectStartingBook}
            selectStartingChapter={this.props.selectStartingChapter}
            selectStartingVerse={this.props.selectStartingVerse}
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

export default DiagramOptionsForm
