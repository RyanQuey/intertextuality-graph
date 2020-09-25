import React from "react"
import { Link } from "gatsby"

import Image from "../components/image"


import {getBookData} from '../helpers/book-helpers'
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
    this.setState({alludingText})
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
    const sourceText = {
      referenceData: parsed,
      valid: osisDataIsValid(parsed),
      osis: osisDataValue(parsed),
    }
    this.setState({sourceText})
  }

  submit (e) {
    e && e.preventDefault && e.preventDefault() 
    const { sourceText, alludingText } = this.state
    console.log("submitting", sourceText, alludingText )

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
    }).then(r => console.log("saved to db: ", r))

	}

  componentDidMount () {
  }

  render () {
    const { sourceText, alludingText } = this.state

    return (
      <div>
        <Form onSubmit={this.submit}>
          <h3>Connect Two Texts</h3>
          Text: ({alludingText.valid ? alludingText.osis : "invalid"})
          <div>
            <Input
              onChange={this.changeAlludingText}
            />
          </div>

          alludes to text  ({sourceText.valid ? sourceText.osis : "invalid"}): 
          <div>
            <Input
              onChange={this.changeSourceText}
            />
          </div>
          <Button
            onClick={this.submit}
            disabled={!sourceText.valid || !alludingText.valid}
          >
            Submit
          </Button>
        </Form>
      </div>
    )
  }
}

export default AddConnectionForm
