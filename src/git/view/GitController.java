package git.view;

import java.nio.file.Path;
import java.util.List;

import git.tools.CommitMessage;
import git.tools.FileState;
import git.tools.GitRepository;

public class GitController {
	private static final String MENU_NAME = "Git menu";
	private GitRepository git;

	public GitController(GitRepository git) {
		this.git = git;
	}

	public Menu getMenu() {
		return new Menu(MENU_NAME, Item.of("Commit", this::commit), Item.of("Info", this::info),
				Item.of("Create a branch", this::createBranch), Item.of("Rename a branch", this::renameBranch),
				Item.of("Delete a branch", this::deleteBranch), Item.of("Log", this::log),
				Item.of("Branches", this::branches), Item.of("Commit content", this::commitContent),
				Item.of("SwitchTo", this::switchTo), Item.of("Get head", this::getHead),
				Item.of("Add an ignored filename expression", this::addIgnoredFileNameExp),
				Item.of("Exit", (io) -> git.save(), true));
	}

	private void commit(InputOutput io) {
		String msg = io.readString("Enter a commit message:");
		io.writeLine(git.commit(msg));
	}

	private void info(InputOutput io) {
		List<FileState> files = git.info();
		if (!files.isEmpty()) {
			io.writeLine("Files:");
			files.forEach(io::writeLine);
		} else {
			io.writeLine("No files in directory.");
		}
	}

	private void createBranch(InputOutput io) {
		String branchName = io.readString("Enter a new branch name:");
		io.writeLine(git.createBranch(branchName));
	}

	private void renameBranch(InputOutput io) {
		String branchName = io.readString("Enter a branch name:");
		String newbranchName = io.readString("Enter a branch new name:");
		io.writeLine(git.renameBranch(branchName, newbranchName));
	}

	private void deleteBranch(InputOutput io) {
		String branchName = io.readString("Enter a branch name:");
		io.writeLine(git.deleteBranch(branchName));
	}

	private void log(InputOutput io) {
		List<CommitMessage> commitMsg = git.log();
		if (!commitMsg.isEmpty()) {
			io.writeLine("Commits:");
			commitMsg.forEach(io::writeLine);
		} else {
			io.writeLine("No commits yet.");
		}
	}

	private void branches(InputOutput io) {
		List<String> branches = git.branches();
		if (!branches.isEmpty()) {
			io.writeLine("Branches:");
			branches.forEach(io::writeLine);
		} else {
			io.writeLine("No files in directory.");
		}
	}

	private void commitContent(InputOutput io) {
		String commitName = io.readString("Enter a name of branch/commit:");
		List<Path> files = git.commitContent(commitName);
		if (!files.isEmpty()) {
			io.writeLine("Files in the branch/commit:");
			files.forEach(io::writeLine);
		} else {
			io.writeLine("No files in the branch/commit.");
		}
	}

	private void switchTo(InputOutput io) {
		String commitName = io.readString("Enter a name of branch/commit:");
		io.writeLine(git.switchTo(commitName));
	}

	private void getHead(InputOutput io) {
		String head = git.getHead();
		io.writeLine(head != null ? head : "Head's on commit");
	}

	private void addIgnoredFileNameExp(InputOutput io) {
		String regex = io.readString("Enter an expression:");
		io.writeLine(git.addIgnoredFileNameExp(regex));
	}

}