package se.natusoft.osgi.aps.api.net.groups.service;

import java.util.List;

/**
 * This service provides information about current groups and members.
 */
public interface APSGroupsInfoService {
    /**
     * Returns the names of all available groups.
     */
    List<String> getGroupNames();

    /**
     * Returns a list of member ids for the specified group.
     *
     * @param groupName The name of the group to get member ids for.
     */
    List<String> getGroupMembers(String groupName);

    /**
     * Returns a list of "groupName : groupMember" for all groups and members.
     */
    List<String> getGroupsAndMembers();
}
