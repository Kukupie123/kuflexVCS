package dev.kukode.beans.commits;

import java.util.UUID;

public class CommitModel {
    String UID; //UID of the commit
    String name; //Name of the commit
    String comment; //Comment of the commit
    String inheritedBranch; //The branch this commit was made from
    String inheritedCommit; // The commit which this commit comes from

    public CommitModel(String name, String comment, String inheritedBranch, String inheritedCommit) {
        this.name = name;
        this.comment = comment;
        this.inheritedBranch = inheritedBranch;
        this.inheritedCommit = inheritedCommit;
        UUID uid = UUID.randomUUID();
        this.UID = uid.toString();
    }
}
