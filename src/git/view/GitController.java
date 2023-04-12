package git.view;

import java.util.List;

import git.tools.FileState;
import git.tools.GitRepository;
import telran.view.InputOutput;

public class GitController {
	private static final String ERROR_MSG = "Wrong input";
	private static final String DATE_FORMAT = "dd.MM.yyyy";
	private static final String MENU_NAME = "Git menu";
	private GitRepository git;

	public GitController(GitRepository git) {
		this.git = git;
	}

	public Menu getMenu() {
		return new Menu(MENU_NAME, Item.of("Commit", this::commit), Item.of("Info", this::info),
				Item.of("Create branch", this::createBranch), Item.of("Rename branch", this::renameBranch),
				Item.of("Delete branch", this::deleteBranch), Item.of("Log", this::log),
				Item.of("Branches", this::branches), Item.of("Commit content", this::commitContent),
				Item.of("switchTo", this::switchTo), Item.of("getHead", this::getHead),
				Item.of("addIgnoredFileNameExp", this::addIgnoredFileNameExp), Item.of("Exit", this::exit, true));
	}

	private void commit(InputOutput io) {
		String msg = io.readString("Enter a commit message:");
		io.writeLine(git.commit(msg));
	}

	private void info(InputOutput io) {
		git.info().forEach(io::writeLine);
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
		git.log().forEach(io::writeLine);
	}
	
	private void branches(InputOutput io) {
		git.branches().forEach(io::writeLine);
	}
	
	private void commitContent(InputOutput io) {
		String commitName = io.readString("Enter a name of branch/commit:");
		git.commitContent(commitName).forEach(io::writeLine);
	}
	
	private void switchTo(InputOutput io) {
		String commitName = io.readString("Enter a name of branch/commit:");
		io.writeLine(git.switchTo(commitName));
	}
}