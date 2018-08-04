import React, { Component } from 'react';
import './App.css';
import './components/APSWebManager'
import APSWebManager from "./components/APSWebManager";

class App extends Component {
  render() {
    return (
      <div className="App">
        <APSWebManager/>
      </div>
    );
  }
}

export default App;
