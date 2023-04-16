package git.tools;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record CommitMessage(String name, String shortName, String message, LocalDateTime date) implements Serializable {

	private static final String PATTERN_FORMAT = "dd-MM-yyyy HH:mm:ss";
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_FORMAT)
			.withZone(ZoneId.systemDefault());
	private static final int COMMIT_SHORT_NAME_LENGHT = 7;

	public CommitMessage(String name, String message, LocalDateTime date) {
		this(name, name.substring(0, COMMIT_SHORT_NAME_LENGHT), message, date);
	}

	public String toString() {
		return String.format("%s | %s: %s", date.format(formatter), shortName, message);
	}
}
