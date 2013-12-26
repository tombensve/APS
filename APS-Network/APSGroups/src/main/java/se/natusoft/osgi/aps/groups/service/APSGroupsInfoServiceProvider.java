package se.natusoft.osgi.aps.groups.service;

import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.internal.protocol.Group;
import se.natusoft.apsgroups.internal.protocol.Groups;
import se.natusoft.apsgroups.internal.protocol.Member;
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsInfoService;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides information about current groups and members.
 */
public class APSGroupsInfoServiceProvider implements APSGroupsInfoService {
    //
    // Private Members
    //

    /** Our config. */
    private APSGroupsConfig config = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSGroupsInfoServiceProvider.
     *
     * @param config The service configuration.
     */
    public APSGroupsInfoServiceProvider(APSGroupsConfig config) {
        this.config = config;
    }

    //
    // Methods
    //

    /**
     * Returns the names of all available groups.
     */
    @Override
    public List<String> getGroupNames() {
        List<String> groupNames = new LinkedList<>();
        groupNames.addAll(Groups.getAvailableGroups());
        return groupNames;
    }

    /**
     * Returns a list of member ids for the specified group.
     *
     * @param groupName The name of the group to get member ids for.
     */
    @Override
    public List<String> getGroupMembers(String groupName) {
        Group group = Groups.getGroup(groupName);
        List<String> members = new LinkedList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        for (Member member : group.getListOfMembers()) {
            if (member.isLocalMember()) {
                members.add(member.getId().toString() + " (localMember=true" +
                        ", lastAnnounced='" + sdf.format(member.lastAnnounced()) + "'" +
                        ", stillKicking=" + member.stillKicking(this.config.getMemberAnnounceInterval()) + ")");
            }
            else {
                members.add(member.getId().toString() + " (localMember=false" +
                        ", lastHeardFrom='" + sdf.format(member.getLastHeardFrom().getLocalTimeDate()) + "'" +
                        ", stillKicking=" + member.stillKicking(this.config.getMemberAnnounceInterval()) + ")");
            }
        }

        return members;
    }

    /**
     * Returns a list of "groupName : groupMember" for all groups and members.
     */
    @Override
    public List<String> getGroupsAndMembers() {
        List<String> groupsAndMembers = new LinkedList<>();
        for (String groupName : getGroupNames()) {
            groupsAndMembers.add(groupName);
            for (String groupMember : getGroupMembers(groupName)) {
                groupsAndMembers.add("  " + groupMember);
            }
        }

        return groupsAndMembers;
    }
}
