package dev.kukode.beans.commits;


import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommitDB {
    public List<CommitModel> commits;
}
