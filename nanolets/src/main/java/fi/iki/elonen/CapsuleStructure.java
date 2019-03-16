package fi.iki.elonen;

import fi.iki.elonen.crypto.CryptoOptions.User;

import java.util.List;
import java.util.Map;

public class CapsuleStructure {
    public byte[] encryptedFileBytes;
    public List<User> totalUsers;
    public int leastNum;
    public byte[] salt;
    public Map<int[], byte[]> subsets;
}
