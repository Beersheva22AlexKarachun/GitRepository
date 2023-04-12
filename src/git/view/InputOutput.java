package git.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public interface InputOutput {
	String readString(String prompt);

	void writeString(Object obj);

	default void writeLine(Object obj) {
		writeString(obj.toString() + "\n");
	}

	default <R> R readObject(String prompt, String errorPrompt, Function<String, R> mapper) {
		boolean running = true;
		R result = null;
		while (running) {
			try {
				String str = readString(prompt);
				result = mapper.apply(str);
				running = false;
			} catch (Exception e) {
				writeLine(errorPrompt + " - " + e.getMessage());
			}
		}
		return result;
	}

	default String readStringPredicate(String prompt, String errorPrompt, Predicate<String> predicate) {
		boolean running = true;
		String result = null;
		while (running) {
			try {
				String str = readString(prompt);
				if (!predicate.test(str)) {
					throw new Exception(str);
				}
				result = str;
				running = false;
			} catch (Exception e) {
				writeLine(errorPrompt + " - " + e.getMessage());
			}
		}
		return result;
	}

	default String readStringOptions(String prompt, String errorPrompt, Set<String> options) {
		return readStringPredicate(prompt, errorPrompt, options::contains);
	}

	default int readInt(String prompt, String errorPrompt) {
		return getNumber(prompt, errorPrompt, Integer::parseInt, 0, Integer.MAX_VALUE);
	}

	default int readInt(String prompt, String errorPrompt, int min, int max) {
		return getNumber(prompt, errorPrompt, Integer::parseInt, min, max);
	}

	default long readLong(String prompt, String errorPrompt, long min, long max) {
		return getNumber(prompt, errorPrompt, Long::parseLong, min, max);
	}

	default LocalDate readDateISO(String prompt, String errorPrompt) {
		boolean running = true;
		LocalDate result = null;
		while (running) {
			try {
				result = LocalDate.parse(readString(prompt));
				running = false;
			} catch (Exception e) {
				writeLine(errorPrompt + " - " + e.getMessage());
			}
		}
		return result;
	}

	default LocalDate readDate(String prompt, String errorPrompt, String format, LocalDate min, LocalDate max) {
		boolean running = true;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
		LocalDate result = null;
		while (running) {
			try {
				result = LocalDate.parse(readString(prompt), dtf);
				if (result.compareTo(min) < 0 || result.compareTo(max) > 0) {
					throw new Exception(String.format("Date must be in range %s - %s", min, max));
				}
				running = false;
			} catch (Exception e) {
				writeLine(errorPrompt + " - " + e.getMessage());
			}
		}
		return result;
	}

	default double readNumber(String prompt, String errorPrompt, double min, double max) {
		return getNumber(prompt, errorPrompt, Double::parseDouble, min, max);
	}

	private <T extends Number & Comparable<T>> T getNumber(String prompt, String errorPrompt,
			Function<String, T> parser, T min, T max) {
		boolean running = true;
		T result = null;
		while (running) {
			try {
				result = parser.apply(readString(prompt));
				if (!isInRange(result, min, max)) {
					throw new Exception(String.format("Number must be in range %s - %s", min, max));
				}
				running = false;
			} catch (Exception e) {
				writeLine(errorPrompt + " - " + e.getMessage());
			}
		}
		return result;
	}

	private <T extends Number & Comparable<T>> boolean isInRange(T number, T min, T max) {
		return number.compareTo(min) >= 0 && number.compareTo(max) <= 0;
	}
}