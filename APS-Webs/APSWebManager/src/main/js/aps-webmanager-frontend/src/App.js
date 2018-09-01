import React, { Component } from 'react';
import './App.css';
import APSWebManager from "./components/APSWebManager";

class App extends Component {
  render() {
    return (
      <div className="App">
        <APSWebManager name={"default"}/>
      </div>
    );
  }
}

export default App;
