# Elementary VCS

## Usage

- `add <path>` -- add file to working area.
- `branch <name>` -- create new branch with given name
- `branch -d <name>` -- delete an existing branch with given name
- `checkout <name>` -- checkout version with given name
- `commit <message>` -- create new commit with given message
- `init` -- init new repository
- `merge <branch_name>` -- merges current branch and branch with given name
- `remove` -- delete repository
- `help` -- call help

##Internal structure

- package `exceptions` contains used exception classes.
- package `git_objects` contains classes with main GitObjects like Blob or Commit.
- package `repository` contains Manager class that operate with Repository class. Repository class has main functions to work with it.
- Two rest classes needed for parsing of comand line.
