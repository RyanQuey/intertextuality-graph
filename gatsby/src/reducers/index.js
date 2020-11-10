import { combineReducers, createStore, applyMiddleware } from 'redux'
import { composeWithDevTools } from 'redux-devtools-extension'
// might or might not use this time around
//import createSagaMiddleware from 'redux-saga'
//import rootSaga from 'shared/sagas'
import Helpers from '../helpers/base-helpers'

import alerts from './alerts'
import errors from './errors'
import forms from './forms'
import user from './user'
import viewSettings from './viewSettings'

const rootReducer = combineReducers({
  alerts,
  errors,
  forms,
  // the current user
  user,
  viewSettings,
})

//const sagaMiddleware = createSagaMiddleware()

const store = createStore(
  rootReducer,
  composeWithDevTools(
  //  applyMiddleware(sagaMiddleware)
  )
)

//sagaMiddleware.run(rootSaga)

export default store
