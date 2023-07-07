#  Kukode's Flex VCS
#### A Mini-Version Control System application with the ability to use many popular Cloud Storage Service such as google drive.

## Technical Note (Reminder)

### In-Depth Road map
#### How will versioning be implemented?
Easiest way we can have versioning is by setting each version as it's own copy.
But then we will have lots of redundant data.
Instead we can have a special file for each file that is going to track it's changes in it.
There needs to be a size limit and files that are too big will be stored as copy and the versions will be tracked down using a special file to keep track.
#### How will commits work?
When we commit we edit the special file for each file and update it 
For large files we make a new copy of the file (if changed) and track it in its special file
#### How will rollbacks,active commit and branches work?
There will be a main file in the repo that is going to track which commit is active, which branch is active etc etc

1. Reading directories and sub directories. (Don't store in memory though)
2. Adding git_ignore feature
3. 