import React from "react"
import { Link } from "gatsby"

import Image from "../components/image"


import {getBookData, osisToBookName} from '../helpers/book-helpers'
import {getChapterData} from '../helpers/chapter-helpers'
import {
  osisDataValue, 
  osisDataIsValid,
  startingBookFromOsis,
  startingChapterFromOsis,
  startingVerseFromOsis,
  endingBookFromOsis,
  endingChapterFromOsis,
  endingVerseFromOsis, 
  osisDataToTestament,
} from '../helpers/text-helpers'
import {createConnection} from '../helpers/connection-helpers'
import Helpers from '../helpers/base-helpers'

import Form from '../components/shared/elements/Form';
import Button from '../components/shared/elements/Button';
import Input from '../components/shared/elements/Input';
import Select from '../components/shared/groups/Select';
import books from '../data/books';
import { bcv_parser as eng_bcv_parser } from "bible-passage-reference-parser/js/en_bcv_parser"
import { bcv_parser as heb_bcv_parser } from "bible-passage-reference-parser/js/he_bcv_parser"

import classes from './scss/add-connection-form.scss'

// TODO note that this might actually only change the language of the book names, might not impact
// versification at all
var eng_bcv = new eng_bcv_parser;
var heb_bcv = new heb_bcv_parser;

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

  changeAlludingText (value) {
    let parser
    if (this.state.versification == "English") {
      parser = eng_bcv
    } else {
      parser = heb_bcv
    }

    //const osisVal = parser.parse(value).osis() 
    const parsed = parser.parse(value).parsed_entities() 
    const alludingText = {
      referenceData: parsed,
      valid: osisDataIsValid(parsed),
      osis: osisDataValue(parsed),
    }
    this.setState({alludingText, formResult: false})
  }

  changeSourceText (value) {
    let parser
    if (this.state.versification == "English") {
      // TODO might need to try
      // https://github.com/openbibleinfo/Bible-Passage-Reference-Parser#versification
      parser = eng_bcv
    } else {
      parser = heb_bcv
    }

    const parsed = parser.parse(value).parsed_entities() 
    const isValid = osisDataIsValid(parsed)
    const sourceText = {
      referenceData: parsed,
      valid: isValid,
      osis: osisDataValue(parsed),
    }
    this.setState({sourceText, formResult: false})

    if (isValid) {
      this.props.onChangeSource(parsed)
    }
  }

  submit (e) {
    e && e.preventDefault && e.preventDefault() 
    const { sourceText, alludingText, formResult } = this.state
    console.log("submitting data to create connection", sourceText, alludingText )

    createConnection({
      sourceText: {
        startingBook: startingBookFromOsis(sourceText.referenceData),
        startingChapter: startingChapterFromOsis(sourceText.referenceData),
        startingVerse: startingVerseFromOsis(sourceText.referenceData),
        endingBook: endingBookFromOsis(sourceText.referenceData),
        endingChapter: endingChapterFromOsis(sourceText.referenceData),
        endingVerse: endingVerseFromOsis(sourceText.referenceData),
        parsed: sourceText.referenceData,
      },
      alludingText: {
        startingBook: startingBookFromOsis(alludingText.referenceData),
        startingChapter: startingChapterFromOsis(alludingText.referenceData),
        startingVerse: startingVerseFromOsis(alludingText.referenceData),
        endingBook: endingBookFromOsis(alludingText.referenceData),
        endingChapter: endingChapterFromOsis(alludingText.referenceData),
        endingVerse: endingVerseFromOsis(alludingText.referenceData),
        parsed: alludingText.referenceData,
      },
      confidenceLevel: 70.0,
    }).then(r => {
      console.log("saved to db: ", r)
      this.setState({formResult: {message: "Success!"}})
      setTimeout(this.props.triggerUpdateDiagram, 1000);

      

    }).catch(err => {
      console.log("failed to create new connection", err.message)
      this.setState({formResult: {message: err && err.message || "Error"}})
    })

	}

  componentDidMount () {
  }

  render () {
    const { sourceText, alludingText, formResult } = this.state
    const invalid = !sourceText.valid || !alludingText.valid
    const message = invalid ? "invalid" : `${alludingText.osis} alludes to ${sourceText.osis}`

    // heb bible for ot, SBL GNT for NT
    const alludingTestament = alludingText.valid && osisDataToTestament(alludingText.referenceData)
    const alludingStepBibleVersion = alludingTestament == "Old Testament" ? "OHB" : "SBLG"
    const sourceTestament = sourceText.valid && osisDataToTestament(sourceText.referenceData)
    const sourceStepBibleVersion = sourceTestament == "Old Testament" ? "OHB" : "SBLG"

    return (
      <div>
        <Form onSubmit={this.submit} className="add-connection-form">
          <h3>Connect Two Texts</h3>
          <h4>{message}</h4>

          <div className="connection-form-fields">

            <div className="connection-form-field-ctn">
              Source text: ({sourceText.valid ? sourceText.osis : "invalid"}): 
              <div className="input-wrapper">
                <Input
                  onChange={this.changeSourceText}
                />
              </div>
              {sourceText.valid ? (
                <iframe src={`https://www.stepbible.org/?q=version=${sourceStepBibleVersion}|reference=${sourceText.osis}&options=NUVGH`} height="400" width="450" title="Source Text STEP Bible Preview"></iframe>
              ) : (
                <div></div>
              )}
            </div>

            <div className="connection-form-field-ctn">
              Alluding Text: ({alludingText.valid ? alludingText.osis : "invalid"})
              <div className="input-wrapper">
                <Input
                  onChange={this.changeAlludingText}
                />
              </div>
              {alludingText.valid ? (
                <iframe src={`https://www.stepbible.org/?q=version=${alludingStepBibleVersion}|reference=${alludingText.osis}&options=NUVGH`} height="400" width="450"  title="Alluding Text STEP Bible Preview"></iframe>
              ) : (
                <div></div>
              )}
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
