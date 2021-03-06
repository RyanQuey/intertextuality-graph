import React from "react"
import { Link } from "gatsby"

import Image from "../image"

import {getBookData, osisToBookName} from '../../helpers/book-helpers'
import {getChapterData} from '../../helpers/chapter-helpers'
import Helpers from '../../helpers/base-helpers'

import Form from '../shared/elements/Form';
import Button from '../shared/elements/Button';
import Input from '../shared/elements/Input';
import Select from '../shared/groups/Select';
import DropCSV from '../shared/groups/DropCSV';

import classes from './scss/add-connection-form.scss'

class UploadCSVForm extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      formResult: false,
      message: "",
      invalid: "",
    }

    this.onSuccess = this.onSuccess.bind(this)
    this.onStart = this.onStart.bind(this)

  }


  onStart (e) {
    this.setState({message: "Starting Upload..."})
	}

  onSuccess (e) {
    this.setState({message: "Successfully uploaded csv!"})
    // see notes on AddConnectionForm. Needs to be slow...
    setTimeout(this.props.triggerUpdateDiagram, 3000);
	}

  componentDidMount () {
  }

  render () {
    const { formResult, invalid, message } = this.state

    // heb bible for ot, SBL GNT for NT

    return (
      <div>
        <Form onSubmit={this.submit} className="add-connection-form">
          <h3>Upload CSV</h3>
          <h4>{message}</h4>

          <div className="connection-form-fields">

            <div className="connection-form-field-ctn">
              <DropCSV 
                label="Upload csv of graph data"
                className=""
                onSuccess={this.onSuccess}
                onStart={this.onStart}
              />
            </div>
          </div>
        </Form>
      </div>
    )
  }
}

export default UploadCSVForm
