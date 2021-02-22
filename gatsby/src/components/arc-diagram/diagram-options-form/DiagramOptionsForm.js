import React from "react"
import _ from "lodash"
import { connect } from 'react-redux'

import Form from '../../shared/elements/Form';
import Button from '../../shared/elements/Button';
import Select from '../../shared/groups/Select';

import HopFieldsSet from "./HopFieldsSet"
import HopFieldSetsContainer from "./HopFieldSetsContainer"

import Helpers from '../../../helpers/base-helpers'
import {
  bookOptions,
  hopsCountOptions,
  dataSetOptions,
  allusionDirectionOptions,
} from '../../../constants/arc-diagram'

import './scss/diagram-options-form.scss'


class DiagramOptionsForm extends React.Component {
  constructor (props) {
    super(props)


    this.state = {

    }
    
    this.submit = this.submit.bind(this)

	}

  submit (e) {
    e && e.preventDefault && e.preventDefault() 
    this.props.refreshChartData()
  }

  componentDidMount () {
  }
  
  render () {
    const { startingBook, startingChapter, startingVerse, allusionDirection, dataSet, hopsCount, hopsParams } = this.props

    // TODO check all hops
    const invalidParams = !Helpers.safeDataPath(hopsParams, "hopSet0.reference.valid", false)

    return (
      <div className={"configForm"}>
        <Form  onSubmit={this.submit}>

          <h2>Now showing:</h2>
          <HopFieldSetsContainer />

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
            small={true}
            rectangle={true}
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
    hopsParams: Helpers.safeDataPath(state.forms, "HopFieldsSet.referenceFilter.params")
  }
}

export default connect(mapStateToProps)(DiagramOptionsForm)
