import React from "react"

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
} from '../../../../helpers/text-helpers'
import Helpers from '../../../../helpers/base-helpers'

import Button from '../../elements/Button';
import Input from '../../elements/Input';
import Select from '../Select';
import { bcv_parser as eng_bcv_parser } from "bible-passage-reference-parser/js/en_bcv_parser"
import { bcv_parser as heb_bcv_parser } from "bible-passage-reference-parser/js/he_bcv_parser"

import './style.scss'

// TODO note that this might actually only change the language of the book names, might not impact
// versification at all
const osisParserOptions = {
  // any books that appear on their own get parsed as the complete book ("Gen" → "Gen.1-Gen.50").
  book_alone_strategy: "full",
  // if book is used in range, count it as starting at first chapter (e.g., "Matt-Mark 2" →
  // "Matt.1-Mark.2", Matt-Mark → matt.1-mark.16)
  book_range_strategy: "include",
}
var eng_bcv = new eng_bcv_parser;
var heb_bcv = new heb_bcv_parser;
eng_bcv.set_options(osisParserOptions)
heb_bcv.set_options(osisParserOptions)

class TextReferenceInput extends React.Component {
  constructor (props) {
    super(props)

    this.changeReference = this.changeReference.bind(this)
  }

  changeReference (value) {
    let parser
    if (this.props.versification == "Hebrew") {
      // TODO might need to try
      // https://github.com/openbibleinfo/Bible-Passage-Reference-Parser#versification
      parser = heb_bcv
    } else {
      parser = eng_bcv
    }

    const parsed = parser.parse(value).parsed_entities() 
    const isValid = osisDataIsValid(parsed)
    const referenceData = {
      parsed,
      valid: isValid,
      osis: osisDataValue(parsed),
      startingBook: isValid && startingBookFromOsis(parsed),
      startingChapter: isValid && startingChapterFromOsis(parsed),
      startingVerse: isValid && startingVerseFromOsis(parsed),
      endingBook: isValid && endingBookFromOsis(parsed),
      endingChapter: isValid && endingChapterFromOsis(parsed),
      endingVerse: isValid && endingVerseFromOsis(parsed),
      testament: isValid && osisDataToTestament(parsed),
      inputValue: value,
    }

    //console.log("reference data", referenceData)
    this.props.onChange(referenceData, value)
  }

  componentDidMount () {
    const { reference } = this.props
    // make sure to only do this on Mount
    if (!reference || Object.keys(reference).length == 0) {
      this.changeReference("Gen.1-2")
    }
  }

  render () {
    const { label, reference, textVersion, showIframe } = this.props
    const inputValue = reference.inputValue

    // set text version by props, or default based on which testament
    const testament = reference.valid && osisDataToTestament(reference.parsed)
    const stepBibleVersion = textVersion || testament == "Old Testament" ? "OHB" : "SBLG"

    return (
      <div>
        <div className="text-reference-input-ctn">
          {label}: ({reference.valid ? reference.osis : "invalid"}): 
          <div className="text-reference-input-wrapper input-wrapper">
            <Input
              onChange={this.changeReference}
              value={inputValue}
            />
          </div>
          {showIframe && (
            <div className="iframe-ctn">
              {reference.valid ? (
                <iframe src={`https://www.stepbible.org/?q=version=${textVersion}|reference=${reference.osis}&options=NUVGH`} className="step-bible-iframe" height="400" width="450" title="Source Text STEP Bible Preview"></iframe>
              ) : (
                <div className="iframe-placeholder"></div>
              )}
            </div>
          )}
        </div>

      </div>
    )
  }
}

export default TextReferenceInput
