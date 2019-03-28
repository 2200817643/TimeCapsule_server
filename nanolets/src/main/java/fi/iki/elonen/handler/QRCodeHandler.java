package fi.iki.elonen.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.zxing.WriterException;
import fi.iki.elonen.AppNanolets;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.Util.QRCodeUtil;
import fi.iki.elonen.crypto.DecryptProcess;
import fi.iki.elonen.crypto.EncryptProcess;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultStreamHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QRCodeHandler extends DefaultStreamHandler {
    @Override
    public NanoHTTPD.Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {

        System.out.println(session.getUri());
        Map<String, String> params = session.getParms();
        int processid = Integer.valueOf(params.get("processid"));

        String contect = "";
        if (params.get("action").equalsIgnoreCase("share_to_ep")) {
            EncryptProcess ep = AppNanolets.instance.getEncryptProcess(processid);
            contect = "/user/addEncryptionUser.html?processid=" + processid ;
        } else {
            DecryptProcess dp = AppNanolets.instance.getDecryptProcess(processid);
            contect = "/user/addDecryptionUser.html?processid=" + processid ;
        }


        try {
            //TODO:不用修改
            System.out.println(AppNanolets.url+contect);
            BufferedImage image = QRCodeUtil.createImage(AppNanolets.url+contect, "", false);
           QRCodeUtil.insertImage(image, "C:\\Users\\QinHuoBin\\Desktop\\Repository\\b.png",true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, QRCodeUtil.FORMAT_NAME, baos);
            String src = "data:image/jpg;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

            JSONObject json = new JSONObject();
            json.put("src", src);
            json.put("contect", contect);
            return NanoHTTPD.newChunkedResponse(getStatus(), getMimeType(), new ByteArrayInputStream(json.toJSONString().getBytes()));
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;

    }

    /**
     * 废弃不用
     */
    @Override
    public NanoHTTPD.Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        try {
            session.parseBody(new HashMap<String, String>());
            Map<String, List<String>> map = session.getParameters();
            System.out.println("收到qrcode请求：" + map);
            String contect = map.get("url").get(0);
            if (contect == null || contect.isEmpty()) {
                return NanoHTTPD.newChunkedResponse(getStatus(), getMimeType(), getData());
            }

            BufferedImage image = QRCodeUtil.createImage(contect, "", false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, QRCodeUtil.FORMAT_NAME, baos);


            String data = "data:image/jpg;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
            return NanoHTTPD.newChunkedResponse(getStatus(), getMimeType(), new ByteArrayInputStream(data.getBytes()));
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException | NanoHTTPD.ResponseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getMimeType() {
        return "text/plain";
    }

    @Override
    public IStatus getStatus() {
        return Status.OK;
    }

    @Override
    public InputStream getData() {
        return new ByteArrayInputStream("a stream of data ;-)".getBytes());
    }
}
