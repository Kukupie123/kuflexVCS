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

# Problems and solutions

## File Diff chain problem

One of the main concerns I had was regarding working with File Content Diff handling.
It's easy to overlook it as it's just storing the difference between two versions of right?
<Br>

1. Well,
   the first of you saving a file diff is easy as all you have to do is copy-paste the file content and save it as a
   diff.
   <br>
2. The second diff save is easy too as you will be comparing the content of the file diff from first commit with the
   file content of the file in the project.<br>
3. But when you try to make the third commit you think you can just compare the file diff from the second commit with
   the file content of the file in the project.
   <br>
4. This will fail terribly as in the second diff
   where you compared the first diff with the content of the file in the project.
   The content of A is simply the copied content of the file as shown in point 1.
5. For the third diff, we can't compare 2nd diff as 2nd diff file contains a structured diff layout defining the diff
   between first diff and second diff.

### Solution

The solution is
to load the diff starting from the first commit to the latest commit we want to load step by step.
This will help us load the file content in order starting from the first commit to the last commit,
we can then create a new file diff if we are creating a new commit.
We must not overwrite the project's file content and instead save it temporarily somewhere and use it for file diff.
<br>
This gets overly complicated with my testing so the ultimate solution I came up
with is to store the File Diffs in a database.
Each File diff will be its own database.
File1 will have a file1DiffDB that will store the diffs it went through.
<br>
We will make use of index
that increments to track the direction of the file diff with 0 being it's first file diff
(first file diff is basically a copy of the file content).
<br>
We will then simply add new diff record as we keep creating new commit.
Since we are using indexes we should be able to back track
to it's first diff and then start loading the content based on diffs.

## Addition/Removing of files in commit

Files can be added or removed as project keeps evolving.
These files can't be removed
as devs may want
to go back to a commit where the repository will need to load a file that does not exist in the current commit.

### Solution

The solution is to take a snapshot of the structure of the project per commit.
<br>
We can then use the snapshot to get the files that need to be loaded and load them from file diff DB.
<br>

File diff DB also saves the original content with it's index value as 0.

## Future: Having a chain of commits to load can be heavy on the computation

Having to always load from initial to last commit has a lot of backtracking, reading, altering,
comparing etc, and this can get heavy.

### Solution

Save state in the middle
where we copy the contents of the entire project and we can use this as a point
when we are need to load commit that are far away from the initial commit.
Saving state per X number of commits in a branch is a good way, but I have not planned this out yet

# Architecture

## Folder structure

1. .KuFlex (Directory)
    1. KuFlexRepo.json (FILE)
    2. SnapshotDB.db (FILE)
    3. BranchDB.db (FILE)
    4. Diffs (Directory)
        1. FileDiff.diff (FILE. Name will be path of the file in encoded format which we can decode)
