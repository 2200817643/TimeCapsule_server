package fi.iki.elonen.handler;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.crypto.CryptoOptions;
import fi.iki.elonen.crypto.EncryptProcess;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class UserHandler extends RouterNanoHTTPD.StaticPageHandler {


    private RouterNanoHTTPD.UriResource uriResource;
    private Map<String, String> urlParams;
    private NanoHTTPD.IHTTPSession session;

    @Override
    protected BufferedInputStream fileToInputStream(File fileOrdirectory) throws IOException {
        if ("exception.html".equals(fileOrdirectory.getName())) {
            throw new IOException("trigger something wrong");
        }
        return super.fileToInputStream(fileOrdirectory);
    }

    @Override
    public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        this.uriResource = uriResource;
        this.urlParams = urlParams;
        this.session = session;
        System.out.println("有请求");
        if (uriResource.getUri().contains("addEncryptionUser")) {
            // 如果不是点“确定”进这个页面的，回到填信息那个页面
            if (session.getParms().getOrDefault("doAction", "false").equalsIgnoreCase("true")) {
                CryptoOptions.User user = new CryptoOptions.User();
                user.setName(session.getParms().get("username"));
                user.setPassword(session.getParms().get("password"));

                CryptoOptions options = uriResource.initParameter(1, CryptoOptions.class);
                try {
                    options.addUser(user);
                } catch (Exception e) {
                    e.printStackTrace();
                    options.setException(e);
                }

                // 如果人数够了，就进行加密
                if (options.getTotal_users().size() == options.getTotalNum()) {
                    new EncryptProcess(options).start();
                }
                return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), "添加成功");
            } else {
                return super.get(uriResource, urlParams, session);
            }
        }
        return super.get(uriResource, urlParams, session);
    }


    @Override
    public String getMimeType() {
        return "text/plain";
    }

}