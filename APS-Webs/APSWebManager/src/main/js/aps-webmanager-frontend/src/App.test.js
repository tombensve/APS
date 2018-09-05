import React from 'react'
import ReactDOM from 'react-dom'
import App from './App'
import { apsObject } from "./Utils"

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<App />, div);
  ReactDOM.unmountComponentAtNode(div);
});

it('shows a readable object', () => {
  let myObj = apsObject({what: "qaz", ever:"wsx"});
  console.log(myObj.display());
  if (myObj.display() !== "{\"what\":\"qaz\",\"ever\":\"wsx\"}") throw new Error("apsObjects .display did not match expected value1");
});
