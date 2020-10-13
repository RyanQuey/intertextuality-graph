import React from "react"
import PropTypes from 'prop-types'
import classes from './style.scss'
import theme from '../../../../theme'

const Card = ({
  className,
  onClick,
  selected,
  background = 'white',
  color = 'text',
  children,
  hover,
  maxWidth,
  height = '300px', //defaults to 300px;
  wrapperClass,
}) => {

  /* 
   * removing to get off of aphrodite for server side rendering
  const styles = StyleSheet.create({
    card: {
      background: theme.color[background],
      'max-width': maxWidth,
      color: theme.color[color],
      height,
      cursor: onClick ? "pointer" : "default",
      ':hover': {
        background: theme.color[hover],
      },
    },
  })
  */
  return (
    <div className={`${classes.wrapper} ${wrapperClass || ""}`}>
      <div
        className={`${className} ${classes.card} ${selected ? classes.selected : ""}`}
        onClick={onClick}
      >
        {children}
      </div>
    </div>
  )
}

Card.propTypes = {
  background: PropTypes.string,
  children: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node
  ]),
  color: PropTypes.string,
  border: PropTypes.string,
}

export default Card

