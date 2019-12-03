import React, { Component } from 'react';
import './App.css';
import APSWebManager from "./components/APSWebManager";
import APSBusAddress from "./APSBusAddress";
import APSEventBus from "./APSEventBus";
import APSLogger from "./APSLogger";
import { APP_NAME } from "./Constants";

class App extends Component {

  constructor() {
    super();

    this.logger = new APSLogger( APP_NAME );

    // Set app name here!
    this.busAddress = new APSBusAddress( APP_NAME );

    APSEventBus.createBus( "default", this.busAddress );

  }

  render() {
    return (
      <div className="App">
        <APSWebManager app={APP_NAME} />
      </div>
    );
  }
}

export default App;
