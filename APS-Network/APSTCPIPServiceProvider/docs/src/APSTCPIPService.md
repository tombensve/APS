# APSTCPIPService
This service provides, in ways of communication, plain simple TCP/IP communication. Users of this service will however have very little contact with the java.net classes. 

The following are the points of this service:

* Simple TCP/IP usage.

* Makes use of an URI to provide what I call a "connection point". tcp:, udp:, and multicast: are supported protocols.

Do note that you do need to have a basic understanding of TCP/IP to use this service!

## Security
Makes use of 2 separate services if available for security: _APSTCPSecurityService_ and _APSUDPSecurityService_. Neither these nor APSTCPIPService makes any assumptions nor demands on the what and how of the security services implementations. The APSTCPSecurityService must provide secure versions of Socket and ServerSocket, while APSUDPSecureService have 2 methods, one to encrypt the data and one to decrypt the data in a DatagramPacket.

APS currently does not provide any implementation of the APS(TCP/UDP)SecurityService.

## Connection Point URIs
The service makes use of URIs to specify where to connect for sending or receiving.

The URI format is this:

&nbsp;&nbsp;&nbsp;&nbsp;__protocol://host:port#fragment,fragment__

Protocols:

&nbsp;&nbsp;&nbsp;&nbsp;__tcp, udp, multicast__

Fragments:

&nbsp;&nbsp;&nbsp;&nbsp;__secure__ - If specified then one of the APS(TCP/UDP)SecurityService services will be used.
 
&nbsp;&nbsp;&nbsp;&nbsp;__async__ (only valid on _tcp_ protocol)

## Examples

### TCP
#### Write

    APSTCPIPService tcpipSvc;
    ...
    tcpipSvc.sendStreamedRequest(new URI("tcp://localhost:9999"), new StreamedRequest() {
        void sendRequest(URI connectionPoint, OutputStream requestStream, InputStream responseStream) throws IOException {
            // write to requestStream ...
    
            // read from response stream ...
        }
    })
    

#### Read

    APSTCPIPService tcpipSvc;
    ...
    tcpipSvc.setStreamedRequestListener(new URI("tcp:localhost:9999"), this);
    ...
    void requestReceived(URI receivePoint, InputStream requestStream, OutputStream responseStream) {
        // Read request from reqStream ...
    
        // Write response to respStream ...
    }

Note that there can only be one listener per URI.

### UDP / Multicast

Since Multicast uses UDP packets there is no difference between host and port connected UDP or Multicast. The only difference is in the URI where "udp://" is specified for UDP packets and "multicast://" is specified for multicast packets.

#### Write

    APSTCPIPService tcpipSvc;
    ...
    bytes[] bytes = "Some data".getBytes();
    tcpipSvc.sendDataPacket(new URI("udp://localhost:9999"),  bytes);

or  

    tcpipSvc.sendDataPacket(new URI("multicast://all-systems.mcast.net:9999"), bytes);

#### READ

    APSTCPIPService tcpipSvc;
    ...
    tcpipSvc.addDataPacketListener(new URI("udp://localhost:9999"), this);
    ...
    void dataBlockReceived(URI receivePoint, DatagramPacket packet) {
        byte[] bytes = packet.getData();
        ...
    }
    

