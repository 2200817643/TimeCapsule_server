package fi.iki.elonen.crypto;

import com.alibaba.fastjson.JSON;
import fi.iki.elonen.CapsuleStructure;
import fi.iki.elonen.NanoFileUpload;
import fi.iki.elonen.crypto.CryptoOptions.User;
import org.apache.commons.fileupload.FileItem;
import org.apache.http.entity.ContentType;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 该类要完成的任务：
 * 1.解析上传的文件（json）
 * 2.解密数据
 * 3.提供明文下载
 */
public class DecryptProcess extends Thread {
    static final String decryptedFile_FIELD_NAME = "DecryptedFile";
    CapsuleStructure capsule;
    FileItem decryptedFile;
    public CryptoOptions options;
    private int processid = -999;


    public DecryptProcess(CryptoOptions options) {
        this.options = options;


    }

    //根据key找到子密钥
    public static byte[] getSubkey(Map<int[], byte[]> subsets, int[] order) {
        boolean flag;
        for (int[] arr : subsets.keySet()) {
            flag = true;
            //如果长度不相等，假
            if (arr.length != order.length) {
                flag = false;
                continue;
            } else {
                //如果某位数不相等，假
                for (int n = 0; n < arr.length; n++) {
                    if (arr[n] != order[n]) {
                        flag = false;
                        break;
                    }
                }
                if (flag)
                    return subsets.get(arr);
            }
        }
        return null;
    }

    /**
     * 1.解析上传的文件（json）
     * @param uploaded
     */
    public void part1(FileItem uploaded){
        capsule = JSON.parseObject(uploaded.get(), CapsuleStructure.class);

        List<User> totalUsers;
        //获取所有参与者
        totalUsers = capsule.totalUsers;
        options.setTotal_users(totalUsers);
        options.setTotalNum(totalUsers.size());
        options.setLeastNum(capsule.leastNum);
    }
    /**
     * 1.得到解密密码
     */
    private byte[] part2() {
//
//        ///////////加入测试参与者
//        List<User> users = new ArrayList<User>();
//        for (int num = 0; num < 5; num++) {
//
//            User u =options.findUser("u" + num);
//            if(u==null){
//                System.out.println("未能找到user!!!!!!!!!!!!!!!");
//            }
//            users.add(u);
//        }
//        options.setDecrypting_users(users);
        List<User> totalUsers, decrypting_users;
        //获取所有参与者
        totalUsers = capsule.totalUsers;
        //获取参与解密的参与者
        decrypting_users = options.getDecrypting_users();

        //获取加密时的排列
        //由于用户在网页上输入的顺序不一定是正序，故先排序
        Collections.sort(decrypting_users);
        int[] order = new int[decrypting_users.size()];
        for (int n = 0; n < decrypting_users.size(); n++) {
            order[n] = decrypting_users.get(n).getId();
        }

        byte[] subkey = getSubkey(capsule.subsets, order);
        System.out.println("到底能不能得到：" + subkey);

        //解密时，应该倒过来逐层解密
        Collections.reverse(decrypting_users);
        List<User> reversd_users = decrypting_users;
        for (User user : reversd_users) {
            try {
                System.out.println("解密前,[0]=" + subkey[0] + ",[1]=" + subkey[1]);
                subkey = user.getCipher(Cipher.DECRYPT_MODE).doFinal(subkey);
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
        }
        System.out.println("解密后,[0]=" + subkey[0] + ",[1]=" + subkey[1]);

        return subkey;
    }

    private FileItem createDecryptedFile() {
        decryptedFile = options.getFileItemFactory().createItem(decryptedFile_FIELD_NAME + processid, ContentType.DEFAULT_BINARY.toString(), false, decryptedFile_FIELD_NAME + "Name" + processid);
        return decryptedFile;
    }

    private FileItem createSubFile(String filename) {
        return options.getFileItemFactory().createItem(processid+":"+filename, ContentType.DEFAULT_BINARY.toString(), false, processid+":"+filename);
    }

    /**
     * 1,解密文件
     */
    private void part3(byte[] mainkey) {

        // 需要更改


        try {
            User user = new User();
            user.setPassword(new String(mainkey));
            user.setSalt(capsule.salt);
            user.setId(999);

            //解密文件，先保存解密文件，再用Zip流处理
            FileItem decryptedFile=createDecryptedFile();
            OutputStream outStream = decryptedFile.getOutputStream();
            Cipher cipher = user.getCipher(Cipher.DECRYPT_MODE);
            CipherOutputStream cos = new CipherOutputStream(outStream, cipher);

//            InputStream is = options.getUploadedFile().getInputStream();
//            while ((read = is.read(buf)) != -1) {
//                cos.write(buf, 0, read);
//            }
            cos.write(capsule.encryptedFileBytes, 0, capsule.encryptedFileBytes.length);
            outStream.flush();
            cos.flush();
            cos.close();
            outStream.close();
            // byte b[]=uploadedFile.get();
            //cos.write(b,0,b.length);
            //cos.write(buf,0,read);
            //is.close();


            //对压缩文件中每一个条目进行解压，即为原来的文件
            byte[] buf = new byte[1024];
            int read;
            List<FileItem> originalfiles=new ArrayList<>();
            ZipInputStream zis=new ZipInputStream(decryptedFile.getInputStream());
            ZipEntry entry;
            while((entry=zis.getNextEntry())!=null){
                String filename=entry.getName();
                FileItem subfile=createSubFile(filename);

                OutputStream os=subfile.getOutputStream();
                while((read=zis.read(buf,0,1024))!=-1){
                    os.write(buf,0,read);
                }
                os.flush();
                os.close();
                originalfiles.add(subfile);
            }

            //问题：这里只有一个文件
           for(FileItem subfile:originalfiles){
               System.out.println("子文件名称："+subfile.getName());
               subfile.write(new File("C:\\Users\\QinHuoBin\\Desktop\\Repository\\encrypt\\a\\"+subfile.getName()));
               System.out.println(subfile.isInMemory());
           }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void run() {
        part3(part2());
    }

    public void addUser(User user) throws Exception {

        options.addUser(user,Cipher.DECRYPT_MODE);
        System.out.println("解密过程id=" + processid + "：添加了用户" + user);
        // 如果人数够了，就进行加密
        if (options.getLeastNum() == options.getDecrypting_users().size()) {
            this.start();
        }
    }

    public void setCapsuleFile(FileItem file) {
        options.setCapsuleFile(file);
    }

    public void setException(Exception e) {
        options.setException(e);
    }

    public void setUploader(NanoFileUpload uploader) {
        options.setUploader(uploader);
    }

    public int getProcessid() {
        return processid;
    }

    public void setProcessid(int processid) {
        this.processid = processid;
    }
}
