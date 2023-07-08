package dev.kukode.beans;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

public class KuflexRepo {

    public KuflexRepo(String projectName, String creatorName, Date creationDate) {
        this.projectName = projectName;
        this.creatorName = creatorName;
        this.creationDate = creationDate;
    }

    String projectName;
    String creatorName;
    Date creationDate;
    List<String> files; //Files that are part of the repository
    String activeBranch; //The currently active branch
    String activeCommit; //The currently active commit we are working with
}
