package org.linwg.lib;

import java.util.List;

/**
 *If permission code could not judging the UI permission enough,
 * Use the implementation class of this interface to make additional judgments
 *
 * @author adr
 */
public interface IPerGrant {

    /**
     * Use additional logic to tell the system if the permissions are met
     * @param perList The permissions of the control
     * @param relation Relationship between permissions
     * @return Whether the permission is passed
     */
    boolean perGrant(List<String> perList ,PerRelation relation);
}
