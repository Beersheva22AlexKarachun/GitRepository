package git.utills;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ByteUtills {
	
	public static String byteArrayToHexString(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		for (byte b : bytes) {
			result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
		}
		return result.toString();
	}

	public static byte[] toBytesArray(Serializable obj) throws IOException {
		try (ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
				ObjectOutputStream objectOutput = new ObjectOutputStream(bytesOutput)) {
			objectOutput.writeObject(obj);
			return bytesOutput.toByteArray();
		}
	}
}
