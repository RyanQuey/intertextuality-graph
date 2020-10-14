import React from 'react'
import PropTypes from 'prop-types'
import { StyleSheet, css } from 'aphrodite'
import theme from '../../../../theme'

const Flexbox = ({ id, align, background, className, direction, justify, flexWrap, wrap, children, name, color, onClick }) => {
  // hack to get around server side rendering issue
  // https://github.com/Khan/aphrodite#server-side-rendering
  // https://joshwcomeau.com/react/the-perils-of-rehydration/#server-side-rendering-101
  // this is a hack to get around gatsby server side rendering in prod. Better is to get off
  // Aphrodite
  const serverSideRendered = typeof window === 'undefined'
    
  let aphroditeStyles = ""
  if (serverSideRendered) {
      const styles = StyleSheet.create({
        flex: {
        backgroundColor: theme.color[background] || background,
        display: 'flex',
        flexDirection: direction,
        justifyContent: justify,
        alignItems: align,
        flexWrap: flexWrap || wrap,
        color: theme.color[color] || color,
      },
    })
    aphroditeStyles = css(styles.flex)
  }

  return (
    <div id={id || name} name={name} className={`${aphroditeStyles} ${className || ''}`} onClick={onClick}>{children}</div>
  )
}

Flexbox.defaultProps = {
  direction: 'row',
  wrap: 'nowrap',
}

Flexbox.propTypes = {
  align: PropTypes.string,
  background: PropTypes.string,
  className: PropTypes.string,
  children: PropTypes.node,
  direction: PropTypes.string,
  justify: PropTypes.string,
  flexWrap: PropTypes.string,
  name: PropTypes.string,
}

export default Flexbox

