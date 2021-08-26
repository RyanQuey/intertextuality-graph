import React from 'react'
import { Component } from 'react'
import Dropzone from 'react-dropzone'
import Flexbox from '../../elements/Flexbox'
import Icon from '../../elements/Icon'
import './style.scss'
import theme from '../../../../theme'
import {uploadCSVFile} from '../../../../helpers/file-io-helpers'
import Helpers from '../../../../helpers/base-helpers'

class DropCSV extends Component {
  constructor(props) {
    super(props)

    this.state = {
      pending: false,
    }

    this.onDrop = this.onDrop.bind(this)
    this.onDragOver = this.onDragOver.bind(this)
  }

  handleError(e, a) {
    console.log(e, a);
    // TODO want better error handling, in cases verses are just slightly off or something. 
  }

  onDrop (acceptedFiles, rejectedFiles) {
    const acceptedFile = acceptedFiles[0]
    const rejectedFileData = rejectedFiles[0]

    if (rejectedFileData) {
      const rejectedFile = rejectedFileData.file
      const errors = rejectedFileData.errors
      let message
      console.log(rejectedFile)
      if (rejectedFile.size > 4*1000*1000) {
        message = "Maximum file size is 4MB"
      } else if (!rejectedFile.type.includes("application/vnd.ms-excel")) {
        message = "File must be a CSV file (ends with .csv)"
      } else {
        message = "Unknown error"
      }

      /*
      alertActions.newAlert({
        title: "Failed to upload:",
        message: message,
        level: "DANGER",
      })
      */

    } else {
      this.setState({pending: true})
      this.props.onStart && this.props.onStart(acceptedFile, rejectedFileData)

      // currently setup for B2
      uploadCSVFile(acceptedFile)
      .then((result) => {
        this.setState({pending: false})
        this.props.onSuccess && this.props.onSuccess(result.fileUrl)
      })
      .catch((err) => {
        console.log("b2 error: ");
        console.log(err);
        this.setState({pending: false})

        let code = Helpers.safeDataPath(err, "error.code", "")
        let message
        if (code === "service_unavailable") { //b2 temporarily overloaded
          message = "Uploading temporarily unavailable; please try again in a couple seconds"
        } else {
          message = "Unknown error; please refresh page and try again"
        }

        /*
        alertActions.newAlert({
          title: "Failed to upload:",
          message: message,
          level: "DANGER",
        })
        */

        this.props.onFailure && this.props.onFailure(err)
      })
      //clear url from browser memory to avoid memory leak
      //TODO might not need; disabling preview
      window.URL.revokeObjectURL(acceptedFile.preview)

    }
  }

  onDragOver () {

  }

  render() {
    return (
      <Flexbox align="center" direction="column" justify="center" className={this.props.className || ""}>
        <Dropzone
          disabled={this.state.pending}
          disablePreview={true}
          multiple={false}
          onDrop={this.onDrop}
          style={this.props.style}
          maxSize={4*1000*1000} //4MB
          onDragOver={this.onDragOver}
          // I guess sometimes will be one, sometimes the other
          // https://stackoverflow.com/questions/7076042/what-mime-type-should-i-use-for-csv
          accept="application/vnd.ms-excel, text/csv"
          preventDropOnDocument={true}
        >
          {({getRootProps, getInputProps}) => {

            return (
              <div {...getRootProps({
                className: `dropzone`,
                activeClassName: "draggingOver",
              })}>
                <Flexbox align="center" direction="column">
                  <div>{this.props.label}</div>
                  <input {...getInputProps()} />
                  {this.state.pending ? <Icon color="black" name="spinner" /> : <Icon color="black" name="picture-o" />}
                </Flexbox>
              </div>
            )
          }}
        </Dropzone>
      </Flexbox>
    )
  }
}

export default DropCSV
