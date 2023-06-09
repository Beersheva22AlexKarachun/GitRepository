package git.tools;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface GitRepository extends Serializable {
	static final String GIT_FILE = ".mygit";
	static final String REPOSITORY = ".";

	String commit(String commitMessage);

	List<FileState> info();

	String createBranch(String branchName);

	String renameBranch(String branchName, String newName);

	String deleteBranch(String branchName);

	List<CommitMessage> log();

	List<String> branches(); // list of branch names

	List<Path> commitContent(String commitName);

	String switchTo(String name); // name is either a commit name or a branch name

	String getHead(); // return null if head refers commit with no branch

	void save(); // saving to .mygit serialization to file (Object Stream)

	String addIgnoredFileNameExp(String regex);

	Set<String> ignoredExps();

	boolean deleteIgnoredExp(String regex);
}
