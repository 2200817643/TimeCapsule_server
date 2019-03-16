package fi.iki.elonen.crypto;

import com.alibaba.fastjson.JSON;
import fi.iki.elonen.CapsuleStructure;
import fi.iki.elonen.crypto.CryptoOptions.User;
import org.apache.commons.fileupload.FileItem;
import org.apache.http.entity.ContentType;

import javax.crypto.*;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 该类要完成的任务：
 * 1.解析上传的文件（json）
 * 2.解密数据
 * 3.提供明文下载
 */
public class DecryptProcess extends Thread {
    private CryptoOptions options;
    CapsuleStructure capsule;
    public DecryptProcess(CryptoOptions options) {
        this.options = options;
    }

    /**
     * 1.解析上传的文件（json）
     * 2.得到解密密码
     */
    private byte[] part1(FileItem uploaded) {
        capsule= JSON.parseObject(uploaded.get(), CapsuleStructure.class);

        List<User> totalUsers, decrypting_users;
        //获取所有参与者
        totalUsers = capsule.totalUsers;
        options.setTotal_users(totalUsers);


        ///////////加入测试参与者
        List<User> users = new ArrayList<User>();
        for (int num = 0; num < 5; num++) {

            User u =options.findUser("u" + num);
            if(u==null){
                System.out.println("未能找到user!!!!!!!!!!!!!!!");
            }
            users.add(u);
        }
        options.setDecrypting_users(users);

        //获取参与解密的参与者
        decrypting_users = options.getDecrypting_users();

        //获取加密时的排列
        //由于用户在网页上输入的顺序不一定是正序，故先排序
        Collections.sort(decrypting_users);
        int[] order = new int[decrypting_users.size()];
        for (int n = 0; n < decrypting_users.size(); n++) {
            order[n] = decrypting_users.get(n).getId();
        }

        byte[] subkey = getSubkey(capsule.subsets,order);
        System.out.println("到底能不能得到：" + subkey);

        //解密时，应该倒过来逐层解密
        Collections.reverse(decrypting_users);
        List<User> reversd_users = decrypting_users;
        for (User user : reversd_users) {
            try {
                System.out.println("解密前,[0]="+subkey[0]+",[1]="+subkey[1]);
                subkey = user.getCipher(Cipher.DECRYPT_MODE).doFinal(subkey);
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
        }
        System.out.println("解密后,[0]="+subkey[0]+",[1]="+subkey[1]);

        return subkey;
    }


    static final String decryptedFile_FIELD_NAME = "DecryptedFile";
    FileItem decryptedFile;
    private FileItem createDecryptedFile() {
        decryptedFile = options.getFileItemFactory().createItem(decryptedFile_FIELD_NAME, ContentType.DEFAULT_BINARY.toString(), false, decryptedFile_FIELD_NAME + "Name");
        return decryptedFile;
    }
    /**
     * 1,解密文件
     */
    private void part2(byte[] mainkey){

        // 需要更改



        try {
            User user=new User();
            user.setPassword(new String(mainkey));
            user.setSalt(capsule.salt);
            user.setId(999);
            OutputStream outStream = createDecryptedFile().getOutputStream();
            Cipher cipher=user.getCipher(Cipher.DECRYPT_MODE);

            CipherOutputStream cos = new CipherOutputStream(outStream, cipher);
            byte[] buf = new byte[1024];
            int read;
//            InputStream is = options.getUploadedFile().getInputStream();
//            while ((read = is.read(buf)) != -1) {
//                cos.write(buf, 0, read);
//            }
            cos.write(capsule.encryptedFileBytes,0,capsule.encryptedFileBytes.length);

            // byte b[]=uploadedFile.get();
            //cos.write(b,0,b.length);
            //cos.write(buf,0,read);
            //is.close();
            outStream.flush();
            cos.close();

            decryptedFile.write(new File("C:\\Users\\QinHuoBin\\Desktop\\Repository\\encrypt\\124124"));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //根据key找到子密钥
    public static  byte[] getSubkey(Map<int[], byte[]> subsets, int[] order) {
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

    @Override
    public void run() {


//        while(!findThread())
//            ;


         part2(part1(options.getUploadedFile()));
    }

    public boolean findThread(){
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup topGroup = group;
        while (group != null) {
            topGroup = group;
            group = group.getParent();
        }
        int estimatedSize = topGroup.activeCount() * 2;
        Thread[] slackList = new Thread[estimatedSize];
// 获取根线程组的所有线程
        int actualSize = topGroup.enumerate(slackList);
// copy into a list that is the exact size
        Thread[] list = new Thread[actualSize];
        System.arraycopy(slackList, 0, list, 0, actualSize);
        System.out.println("Thread list size == " + list.length);
        boolean flag=false;
        for (Thread thread : list) {
            System.out.println(thread.getName());
            if(thread.getName().equalsIgnoreCase("EncryptProcess1")&&!thread.isAlive()){
                flag=true;
            }

        }
        return flag;
    }
}
