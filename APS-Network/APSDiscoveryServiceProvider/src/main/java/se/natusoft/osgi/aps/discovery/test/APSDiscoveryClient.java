/* 
 * 
 * PROJECT
 *     Name
 *         APS Discovery Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This is a simple discovery service to discover other services on the network.
 *         It supports both multicast and UDP connections.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2011-10-31: Created!
 *         
 */
package se.natusoft.osgi.aps.discovery.test;

import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;
import se.natusoft.osgi.aps.discovery.service.event.DiscoveryEventListener;
import se.natusoft.osgi.aps.discovery.service.net.APSMulticastDiscoveryAnnouncer;
import se.natusoft.osgi.aps.discovery.service.net.APSMulticastDiscoveryListenerThread;
import se.natusoft.osgi.aps.discovery.service.net.APSUDPDiscoveryAnnouncer;
import se.natusoft.osgi.aps.discovery.service.net.APSUDPDiscoveryListenerThread;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This is a shell base java tool basically for testing and debugging the discovery service.
 */
public class APSDiscoveryClient implements DiscoveryEventListener {

    private APSLogger logger;

    private APSMulticastDiscoveryListenerThread mcastListener = null;
    private APSMulticastDiscoveryAnnouncer mcastAnnouncer = null;

    private APSUDPDiscoveryListenerThread udpListener = null;
    private APSUDPDiscoveryAnnouncer udpAnnouncer = null;

    private APSDiscoveryClient() {
        this.logger = new APSLogger(System.out);
    }

    private void help() {
        System.out.println("Commands:");
        System.out.println("");
        System.out.println("    mcast <address> >targetPort>");
        System.out.println("    udp-listen <address> <targetPort>");
        System.out.println("    udp-announce <address> <targetPort>");
        System.out.println("");
        System.out.println("    announce-available description id version targetHost targetPort [url]");
        System.out.println("    announce-leaving description id version targetHost targetPort [url]");
        System.out.println("");
        System.out.println("    quit");
        System.out.println("");
    }

    private void shell() throws Exception {
        help();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        boolean done = false;

        while(!done) {
            try {
                System.out.print("> ");
                String cmd = in.readLine().trim();

                if (cmd.equals("quit")) {
                    done = true;
                }
                else if (cmd.startsWith("mcast")) {
                    String[] arg = cmd.split(" ");
                    mcast(arg[1], arg[2]);
                }
                else if (cmd.startsWith("udp-listen ")) {
                    String[] arg = cmd.split(" ");
                    udpListen(arg[1], arg[2]);
                }
                else if (cmd.startsWith("udp-announce ")) {
                    String[] arg = cmd.split(" ");
                    udpAnnounce(arg[1], arg[2]);
                }
                else if (cmd.startsWith("announce-available")) {
                    String args = cmd.substring(19);
                    announceAvailable(args);
                }
                else if (cmd.startsWith("announce-leaving")) {
                    String args = cmd.substring(17);
                    announceLeaving(args);
                }
                else if (cmd.equals("help")) {
                    help();
                }
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }

        if (this.mcastListener != null) {
            this.mcastListener.stop();
        }
        if (this.udpListener != null) {
            this.udpListener.stop();
        }
    }

    private void mcast(String address, String portStr) throws Exception {
        int port = Integer.valueOf(portStr);
        if (this.mcastListener != null) {
            this.mcastListener.stop();
            this.mcastListener.removeDiscoveryEventListener(this);
        }
        this.mcastListener = new APSMulticastDiscoveryListenerThread(address, port, 10, this.logger);
        this.mcastListener.addDiscoveryEventListener(this);
        this.mcastListener.start();
        if (this.mcastAnnouncer != null) {
            this.mcastAnnouncer.stop();
        }
        this.mcastAnnouncer = new APSMulticastDiscoveryAnnouncer(address, port, this.logger);
        this.mcastAnnouncer.start();
    }

    private void udpListen(String address, String portStr) throws Exception  {
        int port = Integer.valueOf(portStr);
        if (this.udpListener != null) {
            this.udpListener.stop();
            this.udpListener.removeDiscoveryEventListener(this);
        }
        this.udpListener = new APSUDPDiscoveryListenerThread(address, port, 10, this.logger);
        this.udpListener.addDiscoveryEventListener(this);
        this.udpListener.start();
    }

    private void udpAnnounce(String address, String portStr) throws Exception  {
        int port = Integer.valueOf(portStr);
        if (this.udpAnnouncer != null) {
            this.udpAnnouncer.stop();
        }
        this.udpAnnouncer = new APSUDPDiscoveryAnnouncer(address, port, this.logger);
        this.udpAnnouncer.start();
    }

    private void announceAvailable(String args) throws Exception {
        String[] arg = args.split(" ");
        ServiceDescription sd = new ServiceDescription();
        sd.setDescription(arg[0]);
        sd.setServiceId(arg[1]);
        sd.setVersion(arg[2]);
        sd.setServiceHost(arg[3]);
        sd.setServicePort(Integer.valueOf(arg[4]));
        if (arg.length == 6) {
            sd.setServiceURL(arg[5]);
        }

        if (this.mcastAnnouncer != null) {
            this.mcastAnnouncer.serviceAvailable(sd);
        }
        if (this.udpAnnouncer != null) {
            this.udpAnnouncer.serviceAvailable(sd);
        }
    }

    private void announceLeaving(String args) throws Exception {
        String[] arg = args.split(" ");
        ServiceDescription sd = new ServiceDescription();
        sd.setDescription(arg[0]);
        sd.setServiceId(arg[1]);
        sd.setVersion(arg[2]);
        sd.setServiceHost(arg[3]);
        sd.setServicePort(Integer.valueOf(arg[4]));
        if (arg.length == 6) {
            sd.setServiceURL(arg[5]);
        }

        if (this.mcastAnnouncer != null) {
            this.mcastAnnouncer.serviceLeaving(sd);
        }
        if (this.udpAnnouncer != null) {
            this.udpAnnouncer.serviceLeaving(sd);
        }
    }

    public void serviceAvailable(ServiceDescription serviceDescription) {
        System.out.println("Available service:");
        System.out.println(serviceDescription.toString());
    }

    public void serviceLeaving(ServiceDescription serviceDescription) {
        System.out.println("Leaving service:");
        System.out.println(serviceDescription.toString());
    }

    public static void main(String[] args) throws Exception {
        APSDiscoveryClient client = new APSDiscoveryClient();
        client.shell();
    }

}
