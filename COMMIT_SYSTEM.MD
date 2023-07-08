
Here is what a repo model should look like :- <br>
1. List<String> files <br>
These files are part of the repository. <br>
These files will be tracked
2. Project name, description, branch list
3. Authors, total commits, etc

Each branch will be a folder and each branch will have a commit. <br>

Commit :- <br>
Commit will be linked to previous commit using commitID and branchID.


# Initialize
When we initialize we will create a ".kuflex" folder.
It will contain the ".kurepo" file that will represent the repository.
It will add add files in the directory and sub directory into the files list
Unless it is defined in the ".kuFlexIgnore" file.