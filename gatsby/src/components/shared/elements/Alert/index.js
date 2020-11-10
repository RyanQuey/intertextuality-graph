import React from "react"
import './style.scss'
import {
  closeAlerts
} from '../../../../actions/alerts'
import _ from "lodash"

const Alert = ({ alert, className, color, ...props }) => {
  let borderStyle = {}
  //TODO: maybe create a parent component, that goes around all of the alerts
  let level
  if (["BUG", "DANGER"].includes(alert.level)) {
    level = 'danger'
  } else if (["SUCCESS", "ALERT"].includes(alert.level)) {
    level = "success"
  } else if (alert.level === "WARNING") {
    level = "warning"
  } else {
    level = "success"
  }

  return (
    <div
      className={`alert alert-${level} ${className}`}
      role="alert"
    >
      <button type="button" className={"closeButton"} onClick={closeAlerts.bind(null, alert.id)} aria-label="Close">
        <span aria-hidden="true">&times;</span>
      </button>
      <span
        onClick={alert.onClick}
      >
        <strong>{alert.title}</strong>&nbsp;{alert.message}
      </span>

    </div>
  )
}

export default Alert
