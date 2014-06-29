/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         Provides network groups where named groups can be joined as members and then send and
 *         receive data messages to the group. This is based on multicast and provides a verified
 *         multicast delivery with acknowledgements of receive to the sender and resends if needed.
 *         The sender will get an exception if not all members receive all data. Member actuality
 *         is handled by members announcing themselves relatively often and will be removed when
 *         an announcement does not come in expected time. So if a member dies unexpectedly
 *         (network goes down, etc) its membership will resolve rather quickly. Members also
 *         tries to inform the group when they are doing a controlled exit. Most network aspects
 *         are configurable. Please note that this does not support streaming! That would require
 *         a far more complex protocol. It waits in all packets of a message before delivering
 *         the message.
 *         
 *         Note that even though this is an OSGi bundle, the jar produced can also be used as a
 *         library outside of OSGi. The se.natusoft.apsgroups.APSGroups API should then be used.
 *         This API has no external dependencies, only this jar is required for that use.
 *         
 *         When run with java -jar a for test command line shell will run where you can check
 *         members, send messages and files.
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
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.apsgroups;

import se.natusoft.apsgroups.internal.protocol.message.Message;
import se.natusoft.apsgroups.internal.protocol.message.MessageListener;

import java.io.*;

/**
 * Utility to (debug) test with.
 */
public class CmdLineTestShell implements MessageListener {

    private static APSGroups apsGroups = null;

    private static MessageListener msgListener = new CmdLineTestShell();

    private static void sendMessage(GroupMember groupMember, String msg) throws IOException {
        Message message = groupMember.createNewMessage();
        DataOutputStream dos = new DataOutputStream(message.getOutputStream());
        dos.writeUTF(msg);
        dos.close();
        groupMember.sendMessage(message);
    }

    private static void sendFile(GroupMember groupMember, File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }
        Message message = groupMember.createNewMessage();
        DataOutputStream dos = new DataOutputStream(message.getOutputStream());
        dos.writeUTF(sb.toString());
        dos.close();
        groupMember.sendMessage(message);
    }

    /**
     * Notification of received message.
     *
     * @param message The received message.
     */
    @Override
    public void messageReceived(Message message) {
        try {
            DataInputStream dis = new DataInputStream(message.getInputStream());
            String msg = dis.readUTF();
            dis.close();
            System.out.println(">>>>>>> " + message.getMemberId() + ":" + message.getId() + " > " + msg);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void help(GroupMember groupMember) {
        System.out.println("This member: " + groupMember.getMemberId());
        System.out.println("Commands:");
        System.out.println("  done - Exists.");
        System.out.println("  send <data> - Sends the data to the other members.");
        System.out.println("  file <path> - Sends the file specified by path.");
        System.out.println("  debug none/normal/high - Set level of debug printouts.");
        System.out.println("  members - Lists information about members.");
        System.out.println("  help - displays this information.");
    }

    public static void main(String[] args) throws IOException {
        apsGroups = new APSGroups();

        apsGroups.connect();

        Debug.level = Debug.DEBUG_NORMAL;


        GroupMember groupMember = apsGroups.joinGroup("test");
        groupMember.addMessageListener(msgListener);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        help(groupMember);
        boolean done = false;
        while (!done) {
            try {
                System.out.print("NGTest> ");
                String cmd = in.readLine();

                if (cmd.startsWith("done")) {
                    done = true;
                }
                else if (cmd.startsWith("send")) {
                    String msg = cmd.substring(5);
                    sendMessage(groupMember, msg);
                }
                else if (cmd.startsWith("file")) {
                    String fileName = cmd.substring(5);
                    File file = new File(fileName);
                    sendFile(groupMember, file);
                }
                else if (cmd.startsWith("members")) {
                    for (String mi : groupMember.getMemberInfo()) {
                        System.out.println(mi);
                    }
                }
                else if (cmd.startsWith("debug")) {
                    String arg = cmd.substring(6).trim();
                    if (arg.equals("none")) {
                        Debug.level = Debug.DEBUG_NONE;
                        System.out.println("Turned off debug output!");
                    }
                    else if (arg.equals("normal")) {
                        Debug.level = Debug.DEBUG_NORMAL;
                        System.out.println("Turned on 'normal' debug output!");
                    }
                    else if (arg.equals("high")) {
                        Debug.level = Debug.DEBUG_HIGH;
                        System.out.println("Turned on 'high' debug output!");
                    }
                }
                else if (cmd.startsWith("help")) {
                    help(groupMember);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        groupMember.removeMessageListener(msgListener);
        apsGroups.leaveGroup(groupMember);

        apsGroups.disconnect();

    }

}
