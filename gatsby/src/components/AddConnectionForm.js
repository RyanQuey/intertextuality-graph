import React from "react"
import { Link } from "gatsby"

import Image from "../components/image"


import {getBookData} from '../helpers/book-helpers'
import {getChapterData} from '../helpers/chapter-helpers'
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
      valid: Helpers.osisDataIsValid(parsed),
      osis: Helpers.osisDataValue(parsed),
    }
    console.log("Alluding text:", parsed)
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
      valid: Helpers.osisDataIsValid(parsed),
      osis: Helpers.osisDataValue(parsed),
    }
    console.log("source text:", parsed)
    this.setState({sourceText})
  }

  componentDidMount () {
  }

  render () {
    const { sourceText, alludingText } = this.state

    return (
      <div>
        <Form>
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
        </Form>
      </div>
    )
  }
}

export default AddConnectionForm
