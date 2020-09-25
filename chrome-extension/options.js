// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

'use strict';

const kButtonColors = ['#3aa757', '#e8453c', '#f9bb2d', '#4688f1', '#fff'];
let page = document.getElementById('buttonDiv');

// make one button for each color. Clicking the button will set the color
function constructOptions(kButtonColors) {
  for (let item of kButtonColors) {
    let button = document.createElement('button');
    button.style.backgroundColor = item;
    button.addEventListener('click', function() {
      chrome.storage.sync.set({color: item}, function() {
        console.log('color is ' + item);

        return true;  // Will respond asynchronously.
      })
	  
    });
    page.appendChild(button);
  }
  

}



constructOptions(kButtonColors);
