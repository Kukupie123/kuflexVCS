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

    public String projectName;
    public String creatorName;
    public Date creationDate;
    public List<String> files; //Files that are part of the repository
    public String activeBranch; //The currently active branch
    public String activeCommit; //The currently active commit we are working with
}
