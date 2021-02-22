import React from "react"
import { Link } from "gatsby"

import Image from "../image"


import books from '../../data/books';

import {getBookData, osisToBookName} from '../../helpers/book-helpers'
import {getChapterData} from '../../helpers/chapter-helpers'
import {
  startingBookFromOsis,
  startingChapterFromOsis,
  startingVerseFromOsis,
  endingBookFromOsis,
  endingChapterFromOsis,
  endingVerseFromOsis, 
  osisDataToTestament,
} from '../../helpers/text-helpers'
import {createConnection} from '../../helpers/connection-helpers'
import Helpers from '../../helpers/base-helpers'

import Form from '../shared/elements/Form';
import Button from '../shared/elements/Button';
import Input from '../shared/elements/Input';
import Select from '../shared/groups/Select';
import TextReferenceInput from '../shared/groups/TextReferenceInput';

import classes from './scss/add-connection-form.scss'

class AddConnectionForm extends React.Component {
  constructor (props) {
    super(props)

    this.changeAlludingText = this.changeAlludingText.bind(this)
    this.changeSourceText = this.changeSourceText.bind(this)
    this.submit = this.submit.bind(this)

    this.state = {
      versification: "English",
      alludingText: {},
      sourceText: {},
      formResult: false,
    }

  }

  changeAlludingText (alludingText) {
    // TODO move into redux store later
    this.setState({alludingText, formResult: false})
  }

  changeSourceText (sourceText) {
    // TODO move into redux store later
    this.setState({sourceText, formResult: false})

    if (sourceText.valid) {
      this.props.onChangeSource && this.props.onChangeSource(sourceText.parsed)
    }
  }

  submit (e) {
    e && e.preventDefault && e.preventDefault() 
    const { sourceText, alludingText, formResult } = this.state
    console.log("submitting data to create connection", sourceText, alludingText )

    createConnection({
      sourceText,
      alludingText,
      confidenceLevel: 70.0,
    }).then(r => {
      console.log("saved to db: ", r)
      this.setState({formResult: {message: `Successfully connected ${sourceText.osis} and ${alludingText.osis}!`}})
      // this is bad...sometimes 1000 is too fast...or even 2000. 
      // I'm guessing it has to index in Solr first???
      setTimeout(this.props.triggerUpdateDiagram, 3000);

      

    }).catch(err => {
      console.log("failed to create new connection", err.message)
      this.setState({formResult: {message: err && err.message || "Error"}})
    })

	}

  componentDidMount () {
  }

  render () {
    const { sourceText, sourceTextValue, alludingText, alludingTextValue, formResult } = this.state
    const invalid = !sourceText.valid || !alludingText.valid
    const message = invalid ? "invalid" : `${alludingText.osis} alludes to ${sourceText.osis}`

    // heb bible for ot, SBL GNT for NT
    const alludingTestament = alludingText.testament
    const alludingStepBibleVersion = alludingTestament == "Old Testament" ? "OHB" : "SBLG"
    const sourceTestament = sourceText.testament
    const sourceStepBibleVersion = sourceTestament == "Old Testament" ? "OHB" : "SBLG"

    return (
      <div>
        <Form onSubmit={this.submit} className="add-connection-form">
          <h3>Connect Two Texts</h3>
          <h4>{message}</h4>

          <div className="connection-form-fields">

            <div className="connection-form-field-ctn">
              <TextReferenceInput 
                label="Source Text"
                onChange={this.changeSourceText}
                reference={sourceText}
                textVersion={sourceStepBibleVersion}
                showIframe={true}
              />
            </div>

            <div className="connection-form-field-ctn">
              <TextReferenceInput 
                label="Alluding Text"
                onChange={this.changeAlludingText}
                reference={alludingText}
                textVersion={alludingStepBibleVersion}
                showIframe={true}
              />
            </div>
          </div>
          <Button
            onClick={this.submit}
            disabled={invalid}
            type="submit"
          >
            Submit
          </Button>
          {formResult && (<div>{formResult.message}</div>)}
        </Form>
      </div>
    )
  }
}

export default AddConnectionForm
