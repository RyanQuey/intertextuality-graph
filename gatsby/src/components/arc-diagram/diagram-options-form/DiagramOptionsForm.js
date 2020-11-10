import React from "react"
import Form from '../../shared/elements/Form';
import Button from '../../shared/elements/Button';
import Select from '../../shared/groups/Select';

import classes from './scss/diagram-options-form.scss'
import HopFieldsSet from "./HopFieldsSet"
import _ from "lodash"
import { connect } from 'react-redux'

import Helpers from '../../../helpers/base-helpers'
import {
  bookOptions,
  hopsCountOptions,
  dataSetOptions,
  allusionDirectionOptions,
} from '../../../constants/arc-diagram'



class DiagramOptionsForm extends React.Component {
  constructor (props) {
    super(props)


    this.state = {

    }
    
    this.submit = this.submit.bind(this)

	}

  submit (e) {
    e && e.preventDefault && e.preventDefault() 
    this.props.refreshData()
  }

  componentDidMount () {
  }
  
  render () {
    const { startingBook, startingChapter, startingVerse, allusionDirection, dataSet, hopsCount, hopsParams } = this.props

    const invalidParams = !Helpers.safeDataPath(hopsParams, "hopSet0.reference.valid", false)

    return (
      <div className={"configForm"}>
        <Form  onSubmit={this.submit}>
          {Object.keys(hopsParams).map((hopKey, index) => {
            const value = hopsParams[hopKey]

            return (
              <HopFieldsSet 
                index={index}
              />
            )

          })}
          <div className="other-configs">
            <div>
              Data Set
              <Select 
                options={dataSetOptions}
                onChange={this.props.selectDataSet}
                currentOption={dataSet}
              />
            </div>
          </div>
          <Button
            onClick={this.submit}
            disabled={invalidParams}
            type="submit"
          >
            Submit
          </Button>
        </Form>

      </div>
    )
  }
}

const mapStateToProps = state => {
  return {
    hopsParams: Helpers.safeDataPath(state.forms, "HopFieldsSet.referenceFilter.params", {hopSet0: {}})
  }
}

export default connect(mapStateToProps)(DiagramOptionsForm)
