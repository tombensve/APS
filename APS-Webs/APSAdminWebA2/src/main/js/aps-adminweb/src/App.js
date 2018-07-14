import React, { Component } from 'react';
import './App.css';
import './components/GuiMgr'
import GuiMgr from "./components/GuiMgr";

class App extends Component {
  render() {
    return (
      <div className="App">
        <GuiMgr/>
      </div>
    );
  }
}

export default App;
