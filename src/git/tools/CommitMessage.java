package git.tools;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

public record CommitMessage(String name, String shortName, String message, LocalDateTime date) implements Serializable {

	public CommitMessage(String name, String message, LocalDateTime date) {
		this(name, name.substring(0, 7), message, date);
	}

	public String toString() {
		return String.format("%s | %s: %s", date, shortName, message);
	}
}
