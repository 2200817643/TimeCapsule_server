package fi.iki.elonen.crypto;

import fi.iki.elonen.NanoFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class CryptoOptions {
    public static final String TYPE = "PBEWithHmacSHA256AndAES_256";

    public CryptoOptions() {
//        SecureRandom random = new SecureRandom();
//        setSalt(random.generateSeed(8));
    }

    public FileItem getUploadedFile() {
        return uploadedFile;
    }

    public FileItemFactory getFileItemFactory() {
        return getUploader().getFileItemFactory();
    }

    public List<User> users() {
        return getTotal_users();
    }

    public String getMainKey() {
        return mainKey;
    }

    public void setMainKey(String mainKey) {
        this.mainKey = mainKey;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public List<User> getTotal_users() {
        return total_users;
    }

    public int getLeastNum() {
        return leastNum;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public void setLeastNum(int leastNum) {
        this.leastNum = leastNum;
    }

    public void setUploadedFile(FileItem uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public NanoFileUpload getUploader() {
        return uploader;
    }

    public void setUploader(NanoFileUpload uploader) {
        this.uploader = uploader;
    }

    public void setTotal_users(List<User> total_users) {
        this.total_users = total_users;
    }
    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public List<User> getDecrypting_users() {
        return decrypting_users;
    }

    public void setDecrypting_users(List<User> decrypting_users) {
        this.decrypting_users = decrypting_users;
    }
    public void addUser(User user) throws Exception {
        if (total_users.size() + 1 > getTotalNum())
            throw new Exception("太多用户了！");

        total_users.add(user);

        if (total_users.size() == getLeastNum()) {
            System.out.println("达到设定的最少人数");
        }
        System.out.println("user = [" + user + "] added");
    }

    public String getCapsulename() {
        return capsulename;
    }

    public void setCapsulename(String capsulename) {
        this.capsulename = capsulename;
    }

    public static class User implements Comparable<User> {
        private int id;
        private String name;
        private String password;
        private byte[] salt;

        public String getPassword() {
            return password;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        private Cipher cipher;

        public Cipher getCipher(int cihper_mode) {
            if (cipher != null) {
                return cipher;
            }
            try {
                if(cihper_mode == Cipher.ENCRYPT_MODE){
                    if(id==4){
                        System.out.println("ENCRYPT_MODE,id=4");
                    }
                    SecureRandom random = new SecureRandom();
                    setSalt(random.generateSeed(16));
                }

if(id==998||id==999){
    System.out.println("----------------------"+password.toCharArray()[0]+password.toCharArray()[1]+password.toCharArray()[2]);
}
                SecretKeyFactory factory = SecretKeyFactory.getInstance(TYPE);
                PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
                SecretKey key = factory.generateSecret(pbeKeySpec);
                 System.out.println("id="+id+",盐："+getSalt()[0]+".."+getSalt()[1]);



                if (cihper_mode == Cipher.ENCRYPT_MODE) {
                    PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(getSalt(), 100,new IvParameterSpec(getSalt()));//参数1.盐，参数2.迭代次数
                    cipher = Cipher.getInstance(TYPE);
                    cipher.init(Cipher.ENCRYPT_MODE, key, pbeParameterSpec);
                } else {
                    cipher = Cipher.getInstance(TYPE);
                    PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(getSalt(), 100,new IvParameterSpec(getSalt()));//参数1.盐，参数2.迭代次数
                    cipher.init(Cipher.DECRYPT_MODE, key, pbeParameterSpec);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return cipher;
        }

        public byte[] getSalt() {
            return salt;
        }

        public void setSalt(byte[] salt) {
            this.salt = salt;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public int compareTo(User o) {
            return this.getId() - o.getId();
        }
    }

    private String capsulename="capsulename_null";

    // 所有参与加密的用户
    private List<User> total_users = new ArrayList<>();
    // 所有参与解密的用户
    private List<User> decrypting_users = new ArrayList<>();
    // 最少的解密用户数量
    private int leastNum;
    // 总共的用户数量，达到这个数目后，开始加密
    private int totalNum;
    // 负责管理文件的类
    private NanoFileUpload uploader;


    private String mainKey;
    private Exception exception;
    // 加密过程中使用的扰码
    private byte[] salt;

    private FileItem uploadedFile;

    public User findUser(String name){
        for(User user: getTotal_users()){
            if(user.getName().equals(name)){
                return user;
            }
        }
        return null;
    }


}
