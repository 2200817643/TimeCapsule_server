package fi.iki.elonen.router;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class TestUpload {
    public static void main(String[] args) throws Exception {
        TestUpload t=new TestUpload();
t.startTest();
}

    public void startTest() {
        Scanner s=new Scanner(System.in);
        while(true){
            String a=s.next();
            try {
                if(a.startsWith("a"))
                    testPostWithMultipartFormUpload1();
                if(a.startsWith("b"))
                    testPostWithMultipartFormUpload2();
            } catch (Exception e) {
                e.printStackTrace();
            }


            System.out.println("重来");
        }

    }

    public void testPostWithMultipartFormUpload1() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String textFileName = "C:\\Users\\QinHuoBin\\Desktop\\sign and verify";
        HttpPost post = new HttpPost("http://localhost:9090/upload?action=upload_to_encrypt");

        executeUpload(httpclient, textFileName, post);
    }

    public void testPostWithMultipartFormUpload2() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String textFileName = "C:\\Users\\QinHuoBin\\Desktop\\Repository\\encrypt\\123123";
        HttpPost post = new HttpPost("http://localhost:9090/upload?action=upload_to_decrypt");

        executeUpload(httpclient, textFileName, post);
    }

    private void executeUpload(CloseableHttpClient httpclient, String textFileName, HttpPost post) throws IOException, ClientProtocolException {
        FileBody fileBody = new FileBody(new File(textFileName), org.apache.http.entity.ContentType.DEFAULT_BINARY);
        StringBody stringBody1 = new StringBody("Message 1", org.apache.http.entity.ContentType.MULTIPART_FORM_DATA);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("upfile", fileBody);
        builder.addPart("text1", stringBody1);
        HttpEntity entity = builder.build();
        //
        post.setEntity(entity);
        HttpResponse response = httpclient.execute(post);
        System.out.println("response" + response.getStatusLine().getStatusCode());
    }
}
