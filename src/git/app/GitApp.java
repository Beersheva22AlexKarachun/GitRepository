package git.app;

import git.tools.GitRepository;
import git.tools.GitRepositoryImpl;
import git.view.GitController;
import git.view.InputOutput;
import git.view.Menu;
import git.view.StandardInputOutput;

public class GitApp {

	public static void main(String[] args) {
		GitRepositoryImpl git = GitRepositoryImpl.init();
		GitController controller = new GitController(git);
		Menu menu = controller.getMenu();
		InputOutput io = new StandardInputOutput();
		menu.perform(io);
		git.commitsSet().forEach(System.out::println);
	}
}
