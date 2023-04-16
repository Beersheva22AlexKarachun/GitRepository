package git.tools;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static git.utills.ByteUtills.*;

public class GitRepositoryImpl implements GitRepository {
	private static final long serialVersionUID = -1294812L;
	private static final int MAX_DEPTH = 1;
	private static final String TEST_STRING = "testString";
	private static final String INITIAL_BRANCH = "master";
	private static final int COMMIT_SHORT_NAME_LENGHT = 7;

	private static final String ALGORITHM = "SHA-1";
	private static MessageDigest digest;

	private String head;
	private final HashMap<String, String> branches = new HashMap<>();
	private final HashMap<String, Commit> commits = new HashMap<>();
	private final HashSet<String> ignoredExps = new HashSet<>();

	public static GitRepositoryImpl init() {
		GitRepositoryImpl git = new GitRepositoryImpl();
		if (Files.exists(Path.of(GIT_FILE))) {
			try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(GIT_FILE))) {
				git = (GitRepositoryImpl) input.readObject();
			} catch (Exception e) {
				new RuntimeException(e);
			}
		}
		try {
			digest = MessageDigest.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			// Unreachable code
		}
		return git;
	}

	@Override
	public String commit(String commitMessage) {
		List<FileState> fileStates = info();
		if (getHead() == null && head != null) {
			return "Unable to commit with no branch.";
		}
		if (!hasFilesToCommit(fileStates)) {
			return "Nothing to commit.";
		}

		Map<String, File> files = fileStates.stream().filter(file -> file.status() != Status.DELETED)
				.map(FileState::name).collect(Collectors.toMap(s -> s, this::getFile));
		String commitName = generateCommitName((Serializable) files);
		String shortCommitName = commitName.substring(0, COMMIT_SHORT_NAME_LENGHT);
		commits.put(shortCommitName, new Commit(commitName, commitMessage, getCommitName(head), files));

		if (head == null) {
			head = shortCommitName;
			createBranch(INITIAL_BRANCH);
		} else {
			branches.replace(head, shortCommitName);
		}
		return String.format("Successfully commited to branch \"%s\".", head);
	}

	private File getFile(String file) {
		try {
			Path filePath = Path.of(file);
			return new File(Files.readAllLines(filePath).toArray(String[]::new),
					Files.getLastModifiedTime(filePath).toInstant());
		} catch (IOException e) {
			// FIXME
			throw new RuntimeException(e);
		}
	}

	private String generateCommitName(Serializable data) {
		try {
			return byteArrayToHexString(digest.digest(toBytesArray(data)));
		} catch (IOException e) {
			// FIXME
			throw new RuntimeException(e);
		}
	}

	@Override
	public String createBranch(String branchName) {
		if (head == null) {
			return "Unable to create branch: there are no commits in the repository.";
		}
		if (branches.putIfAbsent(branchName, getCommitName(head)) == null) {
			head = branchName;
			return String.format("Branch \"%s\" has been created.", branchName);
		} else {
			return String.format("Branch \"%s\"'s already exists.", branchName);
		}
	}

	@Override
	public String renameBranch(String branchName, String newName) {
		String errorMsg = "Unable to rename branch \"%1$s\" to \"%2$s\": ";
		String resMsg = "Branch \"%1$s\" successfully renamed to \"%2$s\".";

		if (!branches.containsKey(branchName)) {
			return String.format(errorMsg + "branch \"%1$s\" doesn't exist.", branchName, newName);
		} else if (branches.containsKey(newName)) {
			return String.format(errorMsg + "branch \"%2$s\" already exists.", branchName, newName);
		}

		branches.put(newName, branches.remove(branchName));
		head = head.equals(branchName) ? newName : head;
		return String.format(resMsg, branchName, newName);
	}

	@Override
	public String deleteBranch(String branchName) {
		String errorMsg = "Unable to delete the branch %s: the branch doesn't exist.";
		String msg = "Branch %s has been deleted.";
		if (branches.containsKey(head) && head.equals(branchName)) {
			return String.format("Unable to delete head.");
		}
		if (branches.remove(branchName) == null) {
			return String.format(errorMsg, branchName);
		}
		Set<String> branchCommits = new HashSet<>();
		branches.keySet().stream().forEach(branch -> {
			String commitName = branches.get(branch);
			while (commitName != null) {
				branchCommits.add(commitName);
				commitName = commits.get(commitName).getPrevCommit();
			}
		});
		String commitName = branches.get(branchName);
		while (!branchCommits.contains(commitName)) {
			commits.remove(commitName);
			commitName = commits.get(commitName).getPrevCommit();
		}

		return String.format(msg, branchName);
	}

	@Override
	public List<String> branches() {
		Set<String> bSet = new HashSet<>(branches.keySet());
		if (bSet.remove(head)) {
			bSet.add("*" + head);
		}
		return new ArrayList<String>(bSet.stream().sorted().toList());
	}

	@Override
	public List<Path> commitContent(String commitName) {
		Commit commit = commits.get(getCommitName(commitName));
		return new ArrayList<Path>(commit != null ? commit.getFiles().keySet().stream().sorted().map(Path::of).toList()
				: Collections.emptyList());
	}

	private String getCommitName(String name) {
		return branches.getOrDefault(name, name);
	}

	@Override
	public String switchTo(String name) {
		if (hasFilesToCommit(info())) {
			return "There are uncommited files: switchTo may only be done after commit.";
		}
		if (head.equals(name)) {
			return String.format("Head's already on %s with name \"%s\".",
					branches.containsKey(head) ? "branch" : "commit", name);
		}
		if (!commits.containsKey(getCommitName(name))) {
			return String.format("Unable to switch: branch/commit with name %s doesn't exist.", name);
		}

		deleteCurrentFiles();
		restoreFilesFromCommit(name);
		head = name;
		return String.format("Successfully switched to %s \"%s\".", branches.containsKey(head) ? "branch" : "commit",
				name);
	}

	private boolean hasFilesToCommit(List<FileState> files) {
		return files.stream().anyMatch(file -> file.status() != Status.COMMITTED);
	}

	private void restoreFilesFromCommit(String name) {
		Commit commit = commits.get(getCommitName(name));
		commit.getFiles().entrySet().forEach(entry -> {
			Path filePath = Path.of(entry.getKey());
			File file = entry.getValue();
			try {
				Files.createFile(filePath);
				Files.write(filePath, Arrays.asList(file.data()));
				Files.setLastModifiedTime(filePath, FileTime.from(file.lastModified()));
			} catch (IOException e) {
				// FIXME
				throw new RuntimeException(e);
			}
		});
	}

	private void deleteCurrentFiles() {
		for (Path file : commitContent(head)) {
			try {
				Files.delete(file);
			} catch (IOException e) {
				// FIXME
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public String getHead() {
		return branches.containsKey(head) ? head : null;
	}

	@Override
	public void save() {
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(GIT_FILE))) {
			output.writeObject(this);
		} catch (Exception e) {
			// FIXME
			throw new RuntimeException(e);
		}
	}

	@Override
	public String addIgnoredFileNameExp(String regex) {
		try {
			TEST_STRING.matches(regex);
			ignoredExps.add(regex);
			return String.format("Regular expression \"%s\"has been added.", regex);
		} catch (PatternSyntaxException e) {
			return "Invalid regular expression.";
		}
	}

	public boolean isFileIgnored(String file) {
		return ignoredExps.stream().anyMatch(file::matches);
	}

	@Override
	public List<FileState> info() {
		List<FileState> files = new ArrayList<>();
		try {
			// get files from directory
			Files.walk(Path.of(REPOSITORY), MAX_DEPTH).map(Path::getFileName).filter(Files::isRegularFile)
					.map(Path::toString).filter(file -> !isFileIgnored(file))
					.map(file -> new FileState(file, getFileStatus(file))).forEach(files::add);

			if (head != null) {
				// get deleted files
				commits.get(getCommitName(head)).getFiles().keySet().stream()
						.filter(file -> !Files.exists(Path.of(file))).map(file -> new FileState(file, Status.DELETED))
						.forEach(files::add);
			}
		} catch (IOException e) {
			// FIXME
			throw new RuntimeException(e);
		}
		Collections.sort(files);
		return files;
	}

	private Status getFileStatus(String file) {
		Commit commit = commits.get(getCommitName(head));
		if (commit == null || !commit.getFiles().containsKey(file)) {
			return Status.UNTRACKED;
		}
		try {
			LocalDateTime commitTime = commit.getDate();
			LocalDateTime fileTime = LocalDateTime.ofInstant(Files.getLastModifiedTime(Path.of(file)).toInstant(),
					ZoneId.systemDefault());
			return commitTime.isBefore(fileTime) ? Status.MODIFIED : Status.COMMITTED;

		} catch (IOException e) {
			// FIXME
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<CommitMessage> log() {
		List<CommitMessage> messages = new ArrayList<>();
		Commit commit = commits.get(getCommitName(head));
		while (commit != null) {
			messages.add(new CommitMessage(commit.getName(), commit.getMessage(), commit.getDate()));
			commit = commits.get(commit.getPrevCommit());
		}
		return messages;
	}

	@Override
	public Set<String> ignoredExps() {
		return new HashSet<String>(ignoredExps);
	}

	@Override
	public boolean deleteIgnoredExp(String regex) {
		return ignoredExps.remove(regex);
	}
	
	public Set<String> commitsSet(){
		return commits.keySet();
	}
}