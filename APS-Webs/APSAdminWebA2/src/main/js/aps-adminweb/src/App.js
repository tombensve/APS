import React, { Component } from 'react';
import './App.css';
import './components/APSGuiMgr'
import APSGuiMgr from "./components/APSGuiMgr";

class App extends Component {
  render() {
    return (
      <div className="App">
        <APSGuiMgr/>
      </div>
    );
  }
}

export default App;
