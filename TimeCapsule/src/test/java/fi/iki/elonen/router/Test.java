package fi.iki.elonen.router;

import fi.iki.elonen.AppNanolets;
import fi.iki.elonen.NanoFileUpload;
import fi.iki.elonen.crypto.CryptoOptions;
import fi.iki.elonen.crypto.CryptoOptions.User;
import fi.iki.elonen.crypto.DecryptProcess;
import fi.iki.elonen.crypto.EncryptProcess;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.http.entity.ContentType;
import org.junit.FixMethodOrder;

import javax.crypto.Cipher;
import java.io.*;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Test {
    AppNanolets instance;
    public static Lock lock = new ReentrantLock();

    static Long start;
    static Long end;
    //开始计时
    public void startcount(){
        start=System.currentTimeMillis();
        System.out.println("正在锁");
        lock.lock();
        System.out.println("锁上了");
    }

    public void freelock(){
        System.out.println("正在解锁");
        lock.unlock();
        System.out.println("解锁了");
    }

    public static  void endcount() throws InterruptedException {
        System.out.println("等待解锁");

        System.out.println("已经解锁");
        end=System.currentTimeMillis();
        System.out.println("秒数:"+(end-start)/1000);
    }
    public static void main(String[] args) throws Exception {
        Test t=new Test();
        t.init();
        t.doDiffNumTest(2,2);
    }
    NanoFileUpload uploader;

    public void init() throws IOException {
        instance =new AppNanolets();
        DiskFileItemFactory factory = new DiskFileItemFactory(1000 * 1024 * 1024, new File("C:\\Users\\QinHuoBin\\Desktop\\Repository"));
        // factory.setRepository();
         uploader = new NanoFileUpload(factory);
    }

    public void doDiffNumTest(int a,int b) throws Exception {
        startcount();
        for(int n=a;n<=b;n++){
            EncryptProcess ep=requestSetup("a",n,(int)Math.ceil(((double)n)/2));
            postTestEncryptingFile(ep,"testFile",100*1024*1024);//100MB
            for(int i=1;i<n;i++){
                requestAddEncryptingUser(ep,i);
            }

        }
        endcount();

    }

/**先来本地直接调用，以后再弄get/post请求*/
    public EncryptProcess requestSetup(String capsulename,int totalnum,int leastnum){
        CryptoOptions options = new CryptoOptions(Cipher.ENCRYPT_MODE);
        options.setCapsulename(capsulename);
        options.setTotalNum(totalnum);
        options.setLeastNum(leastnum);

        EncryptProcess ep = new EncryptProcess(options);
        instance.addEncryptProcess(ep);
        return ep;
    }

    public DecryptProcess requestUnpack(String filepath,int processid) throws IOException {
        CryptoOptions options = new CryptoOptions(Cipher.DECRYPT_MODE);

        DecryptProcess dp = new DecryptProcess(options);
        instance.addDecryptProcess(processid,dp);

        FileItem uploadedcausple = uploader.getFileItemFactory().createItem("testcapsulefile"+"fideldname", ContentType.DEFAULT_BINARY.toString(), false, "testcapsulefile");

        File realcapsulefile=new File(filepath);
        OutputStream os= uploadedcausple.getOutputStream();
        FileInputStream fis=new FileInputStream(realcapsulefile);
        byte[] buf=new byte[1024];
        int read;
        while((read=fis.read(buf,0,1024))!=0){
            os.write(buf,0,read);
        }
        os.flush();
        os.close();

        dp.setCapsuleFile(uploadedcausple);
        dp.options.setCapsulename(uploadedcausple.getName());
        dp.setUploader(uploader);

        //解析，使totaluser之类的出来，才能加用户
        dp.part1(uploadedcausple);

        return dp;
    }

    public void postTestEncryptingFile(EncryptProcess ep,String filename,int length) throws Exception {
        FileItem fileItem = uploader.getFileItemFactory().createItem(filename+"fideldname", ContentType.DEFAULT_BINARY.toString(), false, filename);
        Random r=new Random();

        byte[] k=new byte[1024];
        r.nextBytes(k);
        OutputStream os=fileItem.getOutputStream();
        if(length%1024!=0)
            throw new Exception("文件大小必须为1024的倍数！"+length);
        while(length!=0){
            os.write(k,0,1024);
            length-=k.length;
        }
        os.flush();
        os.close();

        ep.addUploadedFile(fileItem);
        ep.setUploader(uploader);
    }

    public void requestAddEncryptingUser(EncryptProcess ep,int num) throws Exception {
        User user = new User();
        user.setName("username"+num);
        user.setPassword(("password")+num);

            user.setId(ep.getOptions().getTotal_users().size());
            ep.addUser(user);
    }

    public void requestAddDecryptingUser( DecryptProcess dp,int num) throws Exception {
        User user =dp.options.findUser("username"+num);
        dp.addUser(user);
    }

}
