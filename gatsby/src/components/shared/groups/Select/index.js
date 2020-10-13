import React from "react"
import { Component } from 'react'
import PropTypes from 'prop-types'
import theme from '../../../../theme'
import classes from './style.scss'
import Select from 'react-select'


class MySelect extends Component {

  render() {
    const { currentOption, onChange, handleSubmit, options, name, submitButton, label, labelAfter, asynchronous, loadOptions, className, clearable = false, creatable, openOnClick } = this.props

    //might add Select.Async in here too to furtner DRY up? ? TODO
    //haevn't added creatable to async yet TODO
    const SelectTag = creatable ? Select.Creatable : Select

    // TODO note that importing classes like this does not work currently. Just use a plain string
    return (
      <div className={`${classes.selectWrapper} ${className}`}>
        {!labelAfter && (label ? (<label htmlFor={name}>{label}</label>) : null)}
        <div className={classes.selectCtn}>
          {asynchronous ? (
            <Select.Async
              className={`select`}
              name={name}
              id={name}
              onChange={onChange}
              openOnClick={openOnClick}
              loadOptions={loadOptions}
              value={currentOption}
              clearable={clearable}
            />
          ) : (
            <SelectTag
              className={`select`}
              name={name}
              id={name}
              onChange={onChange}
              openOnClick={openOnClick}
              options={options}
              value={currentOption}
              clearable={clearable}
            />
          )}
        </div>

        {submitButton.text
          ? (
            <button type="submit" className={submitButton.classes} onClick={handleSubmit}>
              {submitButton.text}
            </button>
          )
          : null
        }
        {labelAfter && (label ? (<label htmlFor={name}>{label}</label>) : null)}
      </div>
    )
  }
}

MySelect.defaultProps = {
  submitButton: {},
}

MySelect.propTypes = {
  onChange: PropTypes.func.isRequired,
  handleSubmit: PropTypes.func,
  options: PropTypes.array.isRequired,
  name: PropTypes.string,
  value: PropTypes.string,
  submitButton: PropTypes.object,
  label: PropTypes.string,
  labelAfter: PropTypes.bool,
}

export default MySelect

