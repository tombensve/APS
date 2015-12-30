# APSTCPIPService
This service provides, in ways of communication, plain simple TCP/IP communication. Users of this service will however have very little contact with the java.net classes. 

The following are the points of this service:

* Simple TCP/IP usage.

* Remove all host, port, and partly protocol from the client code by only referencing a named configuration provided by the service. 

   * It is however possible to register a temporary config from code and then use it for some flexibility in supporting targets received elsewhere.

* Being able to transparently provide different implementations, like a plain non secure implementation as this is, or an SSL:ed version for TCP. A Test implementation that opens no real sockets nor sends any real packets that can be used by tests are also a possibility.

## Security

This implementation is non secure! It sets the following property on the registered service:

    aps.props.security=nonsecure

## How it works

The service registers an APSConfigService configuration with that service where configurations for TCP, UDP or Multicast connections can be defined. Each configuration entry basically specifies host, port and protocol in addition to a unique name for the entry. Do note that in most cases there needs to be separate entries for clients and services.

The client code should have a configuration of itself that specifies the named entry to use. This name is then passed to the service which then uses the named config. The client just reads/writes without having to care where from or to. 

There is a special feature, when a client tries to use a named configuration that has not been configured nor added by the service code nor other service, then as a last resort the APSSimpleDiscoveryService is called if available to see if it has an entry matching the specified name. If so that is used, otherwise an APSConfigException is thrown. 

## Examples

### TCP
#### Write

    APSTCPIPService tcpipSvc;
    ...
    tcpipSvc.sendTCPRequest("somesvc", new TCPRequest() {
        void tcpRequest(OutputStream requestStream, InputStream responseStream) throws IOException {
            // write to requestStream ...
    
            // read from response stream ...
        }
    })
    

#### Read

    APSTCPIPService tcpipSvc;
    ...
    tcpipSvc.setTCPRequestListener("remotesvc", this);
    ...
    void tcpRequestReceived(String name, InetAddress address, InputStream reqStreamn, OutputStream respStream) throws IOException {
        // Read request from reqStream ...
    
        // Write response to respStream ...
    }


### UDP / Multicast

Since Multicast uses UDP packets there is no difference between host and port connected UDP or Multicast. The only difference is in the configuration where "UDP" is specified for point to point UDP packets and "Multicast" is specified for multicast packets.

#### Write

    APSTCPIPService tcpipSvc;
    ...
    bytes[] bytes = "Some data".getBytes();
    tcpipSvc.sendUDP("myudptarget",  bytes);

#### READ

    APSTCPIPService tcpipSvc;
    ...
    byte[] packetBuff = new byte[4000];
    DatagramPacket packet = tcpipSvc.readUDP("myudpsomething", packetBuff);
    byte[] data = packet.getData(); // This is actually packetBuff being returned!
