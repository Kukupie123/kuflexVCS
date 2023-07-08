
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

# Snapshots and Commits
I am going to keep it super simple.
We will note down the project structure. <br>
We will iterate through each file from the project and use diff algorithm to note down the changes between the active commit and current commit. <br> 
We will Next we save the commit as linked list with the last active commit. <br>
This way we can roll back easily as we will just need to compare the nodes. <br>
#### But how will you handle files that are removed between commits.
When there is a file that existed in commit65 but isn't present in current commit (commit66). 
We can save it in a vault. <br>
So, when we roll back to it, we can get the item back from the vault.
#### Okay, but when you roll back what about the files that you added?
Same concept as above. If we are using current commit (commit65 ) and the next commit (commit66) has a file that
didn't exist in commit65 then we simply store it in a vault and when we change current commit to commit66 we get it back.

#### Nice! So when you move from one branch to another branch, or one commit to another commit with lots of changes how are the extra added or deleted files going to be handled?
Same concept as above. Extras will always be stored in vaults in a structured order.

#### Okay.. Won't there be useless junk data that are not used in any branch or commits?
No, there can't be. I will scan the vault to get rid of files that are not used in any branch or commits.

#### TODO: Define good structured diagram and concept showing how your VCS is going to be designed architecture wise 