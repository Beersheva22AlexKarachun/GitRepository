package git.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.junit.jupiter.api.*;

import git.tools.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GitTest {
	private static final int REPETITION = 20;

	static GitRepositoryImpl git = GitRepositoryImpl.init();

	static String[] fileNames = { "ignoredFile", "passwords", "textFile", "4chan", "deleteAfterFirstCommit" };
	static Set<String> fileNameSet = new HashSet<>(Arrays.asList(fileNames));
	static String[] notIgnoredFiles = { "passwords", "textFile", "deleteAfterFirstCommit" };
	static Set<String> notIgnoredFilesSet = new HashSet<>(Arrays.asList(notIgnoredFiles));
	static String newFile = "newFile";
	static String[] filesToDelete = { "ignoredFile", "passwords", "textFile", "4chan", GitRepository.GIT_FILE,
			newFile, };
	static String[][] fileContents = { { "Java is beautiful", "C++ is fast" }, { "qwerty", "password123", "88888888" },
			{ "The Scanner class is used to get user input, and it is found in the java.util package.",
					"To use the Scanner class, create an object of the class." },
			{}, {} };
	static String[] ignoredExps = { "\\..+", "ignoredFile", "\\d+.+" };

	@BeforeAll
	static void init() {
		createFiles();
		addIgnoredFiles();
	}

	private static void createFiles() {
		for (int i = 0; i < fileNames.length; i++) {
			Path file = Path.of(fileNames[i]);
			try {
				Files.createFile(file);
				Files.write(file, Arrays.asList(fileContents[i]));
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}

	private static void addIgnoredFiles() {
		Arrays.stream(ignoredExps).forEach(regex -> git.addIgnoredFileNameExp(regex));
	}

	@AfterAll
	static void deleteFiles() throws IOException {
		for (String file : filesToDelete) {
			Files.deleteIfExists(Path.of(file));
		}
	}

	@BeforeEach
	void header(TestInfo info) {
		System.out.print("*".repeat(REPETITION));
		System.out.print(info.getDisplayName());
		System.out.println("*".repeat(REPETITION));
	}

	@AfterEach
	void afterEach() {
		System.out.println();
	}

	@Test
	@Disabled
	void saveRestoreGitTest() {
		GitRepository git = GitRepositoryImpl.init();
		assertTrue(git.log().isEmpty());

		git.commit("msg1");
		git.commit("msg2");
		git.commit("msg3");
		git.save();

		assertTrue(Files.exists(Path.of(GitRepository.GIT_FILE)));

		GitRepository git2 = GitRepositoryImpl.init();
		assertFalse(git2.log().isEmpty());
		assertEquals(git.log(), git2.log());
	}

	@Test
	void ignoredEpxsTest() {

		assertTrue(git.isFileIgnored("ignoredFile"));
		assertTrue(git.isFileIgnored(".class"));
		assertTrue(git.isFileIgnored(".settings"));

		assertFalse(git.isFileIgnored("passwords"));
		assertFalse(git.isFileIgnored("text1"));
	}

	@Test
	@Order(1)
	void beforeFirstCommitTest() {
		List<FileState> files = git.info();
		files.forEach(System.out::println);
		assertEquals(notIgnoredFiles.length, files.size());
		files.forEach(file -> {
			assertTrue(notIgnoredFilesSet.contains(file.name()));
			assertEquals(file.status(), Status.UNTRACKED);
		});
		assertNull(git.getHead());
		assertEquals(git.branches().size(), 0);

		assertEquals(0, git.log().size());

		System.out.println(git.createBranch("getters"));

	}

	@Test
	@Order(2)
	void firstCommitTest() {
		String initialBranch = "master";
		System.out.println(git.commit("First commit"));
		assertEquals(initialBranch, git.getHead());

		git.commitContent(initialBranch).forEach(System.out::println);
		git.log().forEach(System.out::println);
		git.info().forEach(System.out::println);
		System.out.println(git.commit("msg"));

		try {
			Files.delete(Path.of("deleteAfterFirstCommit"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	@Order(3)
	void branchesTest() {
		String newBranch1 = "newBranch1";
		String newBranch2 = "newBranch2";

		System.out.println(git.createBranch(newBranch1));
		assertEquals(newBranch1, git.getHead());

		System.out.println(git.createBranch(newBranch2));
		assertEquals(newBranch2, git.getHead());

		assertEquals(git.branches().size(), 3);

		assertIterableEquals(git.commitContent(newBranch1), git.commitContent(newBranch2));

		System.out.println(git.renameBranch(git.getHead(), "host")); // OK
		assertEquals(git.getHead(), "host");

		System.out.println(git.renameBranch(newBranch1, "host")); // Host already exists
		System.out.println(git.renameBranch("defunctBranch", "newName")); // Branch doesn't exist
		git.branches().forEach(System.out::println);

	}

	@Test
	@Order(4)
	void secondCommitTest() {
		try {
			Files.createFile(Path.of(newFile));
			Files.writeString(Path.of(newFile), "new file has been created");

			Files.writeString(Path.of(notIgnoredFiles[0]), "new password", StandardOpenOption.APPEND);
			git.info().forEach(System.out::println);

			System.out.println(git.commit("Second commit at branch \"host\""));
			git.info().forEach(System.out::println);
			git.log().forEach(System.out::println);

			Files.readAllLines(Path.of(notIgnoredFiles[0])).forEach(System.out::println);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	@Order(5)
	void switchToTest() {
		git.info().forEach(System.out::println);
		System.out.println(git.switchTo(git.getHead()));
		List<CommitMessage> commits = git.log();
		String firstCommit = commits.get(commits.size() - 1).name().substring(0, 7);
		String secondCommit = commits.get(commits.size() - 2).name().substring(0, 7);
		System.out.println(git.switchTo(firstCommit));
		git.info().forEach(System.out::println);
		System.out.println(git.commit("msg"));
		System.out.println(git.switchTo(secondCommit));
		git.info().forEach(System.out::println);
		System.out.println(git.commit("msg"));
		System.out.println(git.switchTo("host"));
		System.out.println(git.commit("msg"));
	}

}
