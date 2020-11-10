import React from "react"
import classes from './style.scss'
import { connect } from 'react-redux'
// components/shared/elements/Alert/index.js
import Alert from '../../elements/Alert'
import {
  closeAlerts
} from '../../../../actions/alerts'
import Helpers from '../../../../helpers/base-helpers'
import _ from "lodash"

const Alerts = ({ alerts = []}) => {

  return (
    <div
      className={`alertCtn`}
    >
      {alerts && alerts.map((alert) => {
        return <Alert key={alert.id} alert={alert} />
      })}
    </div>
  )
}

const mapStateToProps = state => {
  return {
    alerts: _.values(state.alerts) || [],
  }
}

const connectedAlerts = connect(mapStateToProps)(Alerts)
export default connectedAlerts
//export default Alerts
