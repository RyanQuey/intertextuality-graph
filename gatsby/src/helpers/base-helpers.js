//import uuid from 'uuidv1';
import moment from 'moment';
import _ from 'lodash';
//TODO remove lib if possible...but other libs might require it or something

/*
import {
  CLEAR_ERRORS,
  HANDLE_ERRORS,
} from 'constants/actionTypes'

import {
  errorActions
} from 'shared/actions'
import {
  newAlert
} from 'shared/actions/alerts'
*/


let Helpers = {
  // extracts the relevant passport profile data from the profile auth data received on login/request, and matches it to the database columns
  // don't think I ever use this...only in nodeHelpers.js
  safeDataPath: function (object, keyString, def = null) {
    let keys = keyString.split('.');
    let returnValue = def;
    let safeObject = object;

    if (!safeObject) {
      return def;
    }

    for (let key of keys) {
      if (safeObject[key]) {
        returnValue = safeObject[key];
        safeObject = safeObject[key];
      } else {
        return def;
      }
    }

    return returnValue;
  },

  /*
  uniqueId: function () {
    return uuid()
  },
  */

  //might do away with...but might be helpful if have lots of fields to iterate over
  handleParam: function (e, key) {
    const objKey = key || e.target.dataset.key
    const obj = {};

    obj[objKey] = e.target.value;

    this.setState(obj);
  },

  //TODO don't use this anymore, just call error actions
  /*
  notifyOfAPIError: (errors, templateName, templatePart, options = {})  => {
    console.log("DEPRECATED_._DON'T_USE_THIS");
    console.log(errors);
    errorActions.handleErrors(errors, templateName, templatePart, options)
  },
  */

  //flattens array of arrays one level
  flatten: (array) => {
    return [].concat.apply([], array)
  },

  // for sending objs as query strings
  // e.g., {a: "a", b: 3} => "a=a&b=3"
  toQueryString: (params) => {
    let paramArr = Object.keys(params).map((param) => {
      let value = params[param]

      // TODO might be good to do something liek encodeURIComponent(p) 
      // https://stackoverflow.com/a/1714899/6952495
      if (typeof value === "object") {
        value = JSON.stringify(value)
      }

      return `${param}=${encodeURIComponent(value)}`
    })
    console.log("param arr", paramArr)
    return `${paramArr.join("&")}`
  },

  // extracts the relevant firebaseData data from the firebase auth data received on login/request
  extractUserData: (firebaseData) => {
    let userData = {
      displayName: firebaseData.displayName,
      email: firebaseData.email,
      photoURL: firebaseData.photoURL,
      uid: firebaseData.uid,
      providerData: firebaseData.providerData,
    }

    return userData
  },
  getBase64: (file) => {
    return new Promise((resolve, reject) => {
      var reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = function () {
        console.log("start of base64", reader.result.slice(0, 15));
        console.log("end of base64", reader.result.slice(-15, -1));
        // remove metadata added to front of string
        let encoded = reader.result.toString().replace(/^data:(.*,)?/, '');
        if ((encoded.length % 4) > 0) {
          encoded += '='.repeat(4 - (encoded.length % 4));
        }
        
        return resolve(encoded)
      };
      reader.onerror = function (error) {
        console.log('Error: ', error);
        return reject(error)
      };
    })
  },

  timestamp: () => moment.utc().format("YYYYMMDDTHHmmss[Z]"),

}

// for adding more helper files to this one
Helpers = Object.assign(Helpers)

export default Helpers
