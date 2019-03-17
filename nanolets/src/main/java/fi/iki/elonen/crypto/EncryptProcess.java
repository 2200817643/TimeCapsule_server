package fi.iki.elonen.crypto;

import com.alibaba.fastjson.JSON;
import fi.iki.elonen.CapsuleStructure;
import fi.iki.elonen.NanoFileUpload;
import fi.iki.elonen.crypto.CryptoOptions.User;
import org.apache.commons.fileupload.FileItem;
import org.apache.http.entity.ContentType;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负责：添加用户，数量够了就开始
 */
public class EncryptProcess extends Thread {
    private CryptoOptions options;
private int processid=-999;
    public EncryptProcess(CryptoOptions options) {
        //设置线程名
        super("EncryptProcess1");
        // test
        this.setOptions(options);


    }

    static final String encryptedFile_FIELD_NAME = "EncryptedFile";
    static final String packedFile_FIELD_NAME = "PackedFile";
    static final String mainPass = "CooL2116NiTh5252";
    FileItem encryptedFile, packedFile;

    private FileItem createEncryptedFile() {
        encryptedFile = getOptions().getFileItemFactory().createItem(encryptedFile_FIELD_NAME, ContentType.DEFAULT_BINARY.toString(), false, encryptedFile_FIELD_NAME + "Name");
        return encryptedFile;
    }

    /*任务1，
     * 1.创建主加密密匙
     * 2.加密文件*/
    public void part1(FileItem uploadedFile) {
        try {
            OutputStream outStream = createEncryptedFile().getOutputStream();
            // 需要更改

            User user=new User();
            user.setPassword(mainPass);
            user.setId(998);
            Cipher cipher=user.getCipher(Cipher.ENCRYPT_MODE);

            getOptions().setSalt(user.getSalt());
//            byte k[] = mainPass.getBytes();
//            SecretKeySpec key = new SecretKeySpec(k, CryptoOptions.TYPE);
//            Cipher enc = Cipher.getInstance(CryptoOptions.TYPE);
           // enc.init(Cipher.ENCRYPT_MODE, key);
            CipherOutputStream cos = new CipherOutputStream(outStream, cipher);
            byte[] buf = new byte[1024];
            int read;
            InputStream is = uploadedFile.getInputStream();
            while ((read = is.read(buf)) != -1) {
                cos.write(buf, 0, read);
            }
            // byte b[]=uploadedFile.get();
            //cos.write(b,0,b.length);
            //cos.write(buf,0,read);
            is.close();
            outStream.flush();
            cos.close();

            getOptions().setMainKey(mainPass);
        } catch (Exception e) {
            e.printStackTrace();
            getOptions().setException(e);
        }
    }

    /*任务2，
     * 1.将用户排列组合，每个组合各加密一次*/
    public Map<int[], byte[]> part2() {
        Map<int[], byte[]> map1 = new HashMap<>();


        System.out.println("subsets");
        ArrayList<int[]> subsets = new ArrayList<>();
        int input[] = new int[getOptions().getTotalNum()];
        for (int n = 0; n < getOptions().getTotalNum(); n++) {
            input[n] = n;
        }
        int output[] = new int[getOptions().getLeastNum()];
        dfs(input, output, 0, 0, subsets);
        System.out.println("subsetsed");

        List<User> users = getOptions().getTotal_users();
        // 从组合中取出子集
        for (int[] a : subsets) {
            byte mainPassCopy[] = mainPass.getBytes();
            // 按照子集中的排列，加密主密钥
            for (int n : a) {
//                if(a[0]==0&a[1]==1&a[2]==2&a[3]==3&a[4]==4){
//                    System.out.println("加密前n="+n+",[0]="+mainPassCopy[0]+",[1]="+mainPassCopy[1]);
//                }
                User user = users.get(n);
                mainPassCopy = encryptWithCipher(user.getCipher(Cipher.ENCRYPT_MODE), mainPassCopy);
            }
//            if(a[0]==0&a[1]==1&a[2]==2&a[3]==3&a[4]==4){
//                System.out.println("加密后,[0]="+mainPassCopy[0]+",[1]="+mainPassCopy[1]);
//                User user = users.get(4);
//            }
            map1.put(a, mainPassCopy);

        }
        System.out.println("密钥加密完成");
        return map1;
    }

    /*任务3，
     * 1.将加密结果打包并返回给文件工厂。*/
    public void part3(Map<int[], byte[]> map1) {
        packedFile = getOptions().getFileItemFactory().createItem(packedFile_FIELD_NAME, ContentType.DEFAULT_BINARY.toString(), false, packedFile_FIELD_NAME + "Name");

        CapsuleStructure capsule=new CapsuleStructure();
        capsule.encryptedFileBytes=encryptedFile.get();
        capsule.totalUsers= getOptions().getTotal_users();
        capsule.leastNum= getOptions().getLeastNum();
        capsule.salt= getOptions().getSalt();
        capsule.subsets=map1;


        String json1 = JSON.toJSONString(capsule);
        //  System.out.println(json1);
        try {
            packedFile.getOutputStream().write(json1.getBytes(), 0, json1.length());
            packedFile.write(new File("C:\\Users\\QinHuoBin\\Desktop\\Repository\\encrypt\\123123"));
        } catch (Exception e) {
            e.printStackTrace();
            getOptions().setException(e);
        }

    }


    public static void dfs(int[] input
            , int[] output, int index, int start, List<int[]> list) {
        if (index == output.length)//产生一个组合序列
            list.add(output.clone()); //
            // System.out.println(Arrays.toString(output));
        else {
            for (int j = start; j < input.length; j++) {
                output[index] = input[j];//记录选取的元素
                dfs(input, output, index + 1, j + 1, list);//选取下一个元素，可选下标区间为[j+1,input.length]
            }
        }
    }


    /**
     * /*生成集合的排列
     * m:总人数
     * n:目标子集人数
     * public List<List<Integer>> subsets(int m,int n) {
     * int nums[]=new int[m];
     * // 初始化从0开始
     * for(int i=0;i<nums.length;i++){
     * nums[i]=i;
     * }
     * List<List<Integer>> list1 = new ArrayList<List<Integer>>();
     * list1.add(new ArrayList<Integer>());
     * // 求取所有子集
     * for(int num:nums){
     * int size=list1.size(); //必须定义在内循环前
     * for(int j=0;j<size;j++){
     * List<Integer> temp = new ArrayList<>(list1.get(j));
     * temp.add(num);
     * list1.add(temp);
     * }
     * }
     * //删掉非必要的子集
     * Iterator<List<Integer>> itr=list1.iterator();
     * while(itr.hasNext()){
     * List a=itr.next();
     * if(a.size()!=n){
     * itr.remove();
     * }
     * }
     * return list1;
     * }
     */

    private byte[] encryptWithCipher(Cipher cipher, byte[] data) {
        try {

            byte[] result = cipher.doFinal(data);
            return result;
//            System.out.println("jdk pbe crypto:" + Base64.getEncoder().encodeToString(result));
//
//            //解密
//            cipher.init(Cipher.DECRYPT_MODE, key, pbeParameterSpec.getParameterSpec());
//            result = cipher.doFinal(result);
//            System.out.println("jdk pbe decrypt:" + new String(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void run() {
        System.out.println("运行中");

        if (getOptions().getTotal_users().size() != getOptions().getTotalNum()) {
            System.out.println(("人不够！"));
        }
        System.out.println("part0");
        part1(getOptions().getUploadedFile());
        System.out.println("part1");
        part3(part2());
        System.out.println("part3");

    }

    public CryptoOptions getOptions() {
        return options;
    }

    public void setOptions(CryptoOptions options) {
        this.options = options;
    }

    public void addUser(User user) throws Exception {
        options.addUser(user);
        System.out.println("加密过程id="+processid+"：添加了用户"+user);
        // 如果人数够了，就进行加密
        if (options.getTotal_users().size() == options.getTotalNum()) {
            this.start();
        }
    }

    public void setUploadedFile(FileItem file){
        options.setUploadedFile(file);
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