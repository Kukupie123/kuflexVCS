
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
1. Create a folder called .kuflex
2. create a file inside it .kuflexrepo with basic info.
3. Do a first automated commit and create a default branch called "Default". Figuring diff for both file and directory is going to be its own monster.
4. Devs will be responsible for ignoring files by creating their own .KuFlexignore file in project root

