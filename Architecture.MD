# KuFlex VCS
KuFlexVCS is a Version Control System similar to Git and other popular VCS application BUT with the added benefit of being able to use popular Cloud Storage Service (Google Drive, Mega.io, ICloud, DropBox) as your Storage Platform.

# Features
These are the features that I have planned and ready to implement. 
1. Repository System
   1. A repository holds information about the repository such as Project Name, creator, Current Commit, Current Branch, Creator etc.
   2. We will also have a way to ignore files that we do not want to be part of the repository.
2. Branches
   1. We can have multiple branches just like other VCS.
3. Commits
   1. Branches will have commits.
4. Rollbacks and switching commits
   1. We will be able to roll back changes and switch to different commits from different branches.
5. Integration with Cloud storage platform as remote Repo

Features that I will implement late into development :- <br>
1. Cloud Platform as Repository hub
2. Handling conflicts

# Structure
## Folder Structure
![dirstructure.jpg](external_resources/dirstructure.jpg)
## Models
These models may go through changes as time goes
```
KuFlexRepo model
{
Project Name;
Creation Date;
Creator Name;
Rest of the details will get updated as time goes
}
```
```
Branch model
{
Name;
UID;
InheritedFrom; 
//From where was this branch created
//Which Branch and Commit did this branch out from

//When creating a new branch it needs to automatically generate an initial commit.
}
```
```
Commit model
{
Commit Name;
Commit Comment;
UID;
InheritedFrom;
//From which Commit was this inherited from.
//The inheritedFrom has to be the from the same branch.
}
```
## Architecture and workings
## Instruction flow examples