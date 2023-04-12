package git.view;

import git.tools.GitRepository;

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
				Item.of("Info", this::info), Item.of("Info", this::info), Item.of("Info", this::info),
				Item.of("Info", this::info), Item.of("Info", this::info), Item.of("Info", this::info),
				Item.of("Info", this::info), Item.of("Info", this::info), Item.of("Info", this::info),
				Item.of("Info", this::info), Item.exit());
	}
}