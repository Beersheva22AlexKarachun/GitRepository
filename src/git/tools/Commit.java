package git.tools;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

public class Commit implements Serializable {

	private static final long serialVersionUID = 4863127995743013980L;

	private final String name;
	private final String message;
	private final String prevCommit;
	private final Map<String, File> files;
	private final LocalDateTime date = LocalDateTime.now();

	public Commit(String name, String message, String prevCommit, Map<String, File> files) {
		this.name = name;
		this.message = message;
		this.prevCommit = prevCommit;
		this.files = files;
	}

	public String getName() {
		return name;
	}

	public String getMessage() {
		return message;
	}

	public String getPrevCommit() {
		return prevCommit;
	}

	public Map<String, File> getFiles() {
		return files;
	}

	public LocalDateTime getDate() {
		return date;
	}
}
