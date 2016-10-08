package com.callba.phone.ui.ease;

import com.hyphenate.EMGroupChangeListener;

/**
 * 群组被解散或者被T监听
 *
 */
public abstract class EaseGroupRemoveListener implements EMGroupChangeListener {

    @Override
    public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onApplicationReceived(String groupId, String groupName, String applyer, String reason) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onApplicationAccept(String groupId, String groupName, String accepter) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onApplicationDeclined(String groupId, String groupName, String decliner, String reason) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onGroupDestroyed(String s, String s1) {

    }

    @Override
    public void onInvitationAccepted(String s, String s1, String s2) {

    }

    @Override
    public void onUserRemoved(String s, String s1) {

    }

    @Override
    public void onInvitationDeclined(String groupId, String invitee, String reason) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage) {
        // TODO Auto-generated method stub
    }
}
