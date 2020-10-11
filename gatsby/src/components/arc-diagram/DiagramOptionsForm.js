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

	}

  componentDidMount () {
  }


 

  render () {
    const { startingBook, startingChapter, startingVerse, chapterOptions, verseOptions, allusionDirection, dataSet, hopsCount } = this.props



    return (
      <div className={"configForm"}>
        <Form>
          <div className="ref-selects-configs">
            <h2>Now showing:</h2>
            <div className="ref-selects">
              <div>
                Texts that allude to &nbsp;
              </div>
              <Select 
                options={bookOptions}
                className="book-select"
                onChange={this.props.selectStartingBook}
                currentOption={startingBook}
              />

              {chapterOptions && (
                <Select 
                  options={chapterOptions}
                  onChange={this.props.selectStartingChapter}
                  currentOption={startingChapter}
                />
              )}
              {verseOptions && (
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
                options={hopsCountOptions}
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
            <div>
              Alludes or Alluded to?
              <Select 
                options={allusionDirectionOptions}
                onChange={this.props.selectAllusionDirection}
                currentOption={allusionDirection}
              />
            </div>

          </div>
        </Form>

      </div>
    )
  }
}

export default DiagramOptionsForm
