import Helpers from '../helpers/base-helpers'
import _ from "lodash"
import uuidv4 from 'uuidv4'
import {
  SET_PARAMS,
  SET_OPTIONS,
  FORM_PERSISTED,
  CLEAR_PARAMS,
  SET_CURRENT_POST,
  SET_CURRENT_POST_TEMPLATE,
  UPLOAD_AUDIO_REQUEST, 
} from '../constants/action-types'
import store from "../reducers"
import axios from "axios"

//override will totally override whatever params are there; otherwise, will just be merged to state
export const setParams = (component, form, params, dirty = true, override) => {
  const payload = {
    component,
    form,
    params,
    dirty,
    override,
  }

  store.dispatch({type: SET_PARAMS, payload})
}

export const setOptions = (component, form, options) => {
  const payload = {
    component,
    form,
    options,
  }

  store.dispatch({type: SET_OPTIONS, payload })
}

const sendAsBase64 = true
const sendAsFormData = false
const sendAsJSON = true


export const uploadAudioFile = (file, cb, onFailure, onStartUploading) => {
  // TODO move the async stuff to saga, make thsi cleaner probabl
  store.dispatch({
    type: UPLOAD_AUDIO_REQUEST,
    payload: file,
    cb,
    onFailure, 
    onStartUploading, 
  })
}


// workaround for when using Heroku Hobby dyno, to wake it up when we're about to hit it
export const pingHobbyServer = () => {
  return axios.post("/wake-up/", {"please-wake-up": "thanks"}, {
    headers: {
      'Content-Type': sendAsJSON ? 'application/json' : 'x-www-form-urlencoded',
    }
  })
  .then((result) => {
    console.log("should be awake now...");

    return result
  })
  .catch((err) => {
    // swallow error...this would be a terrible thing to break things over
    console.error("fail ping hobby heroku dyno");
    console.error(err);
  })
}

//makes it not dirty anymore
export const formPersisted = (component, form) => {
  store.dispatch({
    type: FORM_PERSISTED,
    payload: {
      component,
      form,
    },
  })
}
export const clearParams = (component, form) => {
  store.dispatch({
    type: CLEAR_PARAMS,
    payload: {
      component,
      form,
    },
  })
}

