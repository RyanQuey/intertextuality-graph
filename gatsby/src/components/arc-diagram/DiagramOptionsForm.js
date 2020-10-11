import React from "react"
import Form from '../shared/elements/Form';
import Button from '../shared/elements/Button';
import Select from '../shared/groups/Select';

import classes from './scss/diagram-options-form.scss'

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
          <div className="ref-selects-configs">
            <h2>Now showing:</h2>
            <div>
              {chapterOptions && (
                <Button onClick={this.props.toggleFilterByChapter}>{filterByChapter ? "Filter by Book Only" : "Filter by Chapter"}</Button>
              )}
              {filterByChapter && verseOptions && (
                <Button onClick={this.props.toggleFilterByVerse}>{filterByVerse ? "Filter by Chapter Only" : "Filter by Verse"}</Button>
              )}
            </div>
            <div>
              <Select 
                options={allusionDirectionOptions}
                onChange={this.props.selectAllusionDirection}
                currentOption={allusionDirection}
              />
            </div>
            <div className="ref-selects">
              <Select 
                options={bookOptions}
                className="book-select"
                onChange={this.props.selectStartingBook}
                currentOption={startingBook}
              />

              {filterByChapter && chapterOptions && (
                <Select 
                  options={chapterOptions}
                  onChange={this.props.selectStartingChapter}
                  currentOption={startingChapter}
                />
              )}
              {filterByVerse && verseOptions && (
                <Select 
                  options={verseOptions}
                  onChange={this.props.selectStartingVerse}
                  currentOption={startingVerse}
                />
              )}
            </div>
          </div>
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
