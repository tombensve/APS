import React, { Component } from 'react';
import './App.css';
import APSWebManager from "./components/APSWebManager";
import APSBusAddress from "./APSBusAddress";
import APSEventBus from "./APSEventBus";
import APSLogger from "./APSLogger";

class App extends Component {

  constructor() {
    super();

    this.logger = new APSLogger( "App" );

    // Set app name here!
    this.busAddress = new APSBusAddress( "aps-web-manager" );

    APSEventBus.createBus( "default", this.busAddress );

  }

  render() {
    return (
      <div className="App">
        <APSWebManager app={"demo"} />
      </div>
    );
  }
}

export default App;
