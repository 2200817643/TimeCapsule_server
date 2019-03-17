package fi.iki.elonen.handler;

import fi.iki.elonen.AppNanolets;
import fi.iki.elonen.NanoFileUpload;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.crypto.CryptoOptions;
import fi.iki.elonen.crypto.CryptoOptions.User;
import fi.iki.elonen.crypto.DecryptProcess;
import fi.iki.elonen.crypto.EncryptProcess;
import fi.iki.elonen.router.RouterNanoHTTPD;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import javax.crypto.Cipher;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.decodeParameters;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import static fi.iki.elonen.router.RouterNanoHTTPD.*;

/**
 * 负责接受上传的文件，并加入options里面
 */
public class UploadHandler extends DefaultStreamHandler {
    public NanoHTTPD.Response response = newFixedLengthResponse("");

    public String uri;

    public NanoHTTPD.Method method;

    public Map<String, String> header;


    public Map<String, List<FileItem>> files;

    public Map<String, List<String>> decodedParamters;

    public Map<String, List<String>> decodedParamtersFromParameter;

    public String queryParameterString;


    public UploadHandler() {
        DiskFileItemFactory factory = new DiskFileItemFactory(1000 * 1024 * 1024, new File("C:\\Users\\QinHuoBin\\Desktop\\Repository"));
        // factory.setRepository();
        uploader = new NanoFileUpload(factory);
    }

    @Override
    public String getMimeType() {
        return "text/plain";
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }

    @Override
    public InputStream getData() {
        return null;
    }

    NanoFileUpload uploader;

    @Override
    public NanoHTTPD.Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        Map<String, String> params=session.getParms();
        try {

            files = new HashMap<String, List<FileItem>>();
            FileItem uploadedFile = null;



//            Random random = new Random();
//
//            random.nextBytes(bytes);
//            System.out.println("随机化完成");

            //uploader.set
//            FileItem fakefile = uploader.getFileItemFactory().createItem("a", ContentType.DEFAULT_BINARY.toString(), false, "aa");
//            OutputStream os = fakefile.getOutputStream();
//            os.write(bytes, 0, length);
//            os.flush();
//            os.close();
            //  byte a[]=new byte[i*10*1024*1024];
            //判断上传的文件要加密还是解密
            if (params.getOrDefault("action", "upload_to_encrypt").equalsIgnoreCase("upload_to_encrypt")) {
                System.out.println("upload_to_encrypt");

                int processid=Integer.valueOf(params.get("processid"));
                EncryptProcess ep= AppNanolets.instance.getEncryptProcess(processid);


                ep.setUploadedFile(uploadedFile);
                ep.setUploader(uploader);

            }else{
                System.out.println("upload_to_decrypt");
                int processid=Integer.valueOf(params.get("processid"));
                DecryptProcess dp= AppNanolets.instance.getDecryptProcess(processid);


                dp.setUploadedFile(uploadedFile);
                dp.setUploader(uploader);
            }


//
//
//            // 通用性
//            RouterNanoHTTPD instance=uriResource.initParameter(RouterNanoHTTPD.class);
//            instance.addRoute("/addEncryptionUser/(.)+", UserHandler.class,new File(
//                    "C:\\Users\\QinHuoBin\\Desktop\\网页设计\\").getAbsoluteFile(),options);
//            instance.addRoute("/addEncryptionUser", UserHandler.class,new File(
//                    "C:\\Users\\QinHuoBin\\Desktop\\网页设计\\").getAbsoluteFile(),options);
//            //


        } catch (Exception e) {
            e.printStackTrace();
        }
        this.queryParameterString = session.getQueryParameterString();
        this.decodedParamtersFromParameter = decodeParameters(this.queryParameterString);
        this.decodedParamters = decodeParameters(session.getQueryParameterString());
        return this.response;
    }

    private void addTestUsers(int i,int cipher_mode, List<User> users) {
        for (int num = 0; num < i; num++) {
            User u = new User();
            u.setId(num);
            u.setName("u" + num);
            u.setPassword(num + "u_password");
            // 这是为了在加用户的时候就创建cipher
            u.getCipher(cipher_mode);
            users.add(u);

        }
    }

}
