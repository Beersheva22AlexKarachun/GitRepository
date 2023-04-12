package git.tools;

import java.io.Serializable;
import java.time.Instant;

public record File(String[] data, Instant lastModified) implements Serializable {

}
