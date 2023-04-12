package git.tools;

import java.io.Serializable;

public record FileState(String name, Status status) implements Serializable, Comparable<FileState> {
	private static final long serialVersionUID = 1300706547174857573L;

	@Override
	public String toString() {
		return String.format("%s - %s", name, status);
	}

	@Override
	public int compareTo(FileState o) {
		return name.compareTo(o.name);
	}
}
