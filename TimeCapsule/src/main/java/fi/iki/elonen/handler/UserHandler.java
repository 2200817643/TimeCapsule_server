package fi.iki.elonen.handler;

import fi.iki.elonen.AppNanolets;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.crypto.CryptoOptions.User;
import fi.iki.elonen.crypto.DecryptProcess;
import fi.iki.elonen.crypto.EncryptProcess;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.IHTTPSession;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import static fi.iki.elonen.router.RouterNanoHTTPD.StaticPageHandler;
import static fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

/**
 * 负责管理创建胶囊后的添加用户
 */
public class UserHandler extends StaticPageHandler {

//
//    String a = "<!doctype html>\n" +
//            "<html>\n" +
//            "<head>\n" +
//            "<meta charset=\"utf-8\">\n" +
//            "<title>添加成功</title>\n" +
//            "\n" +
//            "<link href=\"../default/jquery.mobile.theme-1.4.5.css\" rel=\"stylesheet\">\n" +
//            "<link href=\"../default/jquery.mobile.icons-1.4.5.min.css\" rel=\"stylesheet\">\n" +
//            "<link href=\"../default/jquery.mobile.structure-1.4.5.min.css\" rel=\"stylesheet\">\n" +
//            "\n" +
//            "<link href=\"setup.css\" rel=\"stylesheet\">\n" +
//            "<script src=\"../default/jquery-1.12.4.min.js\"></script>\n" +
//            "<script>\n" +
//            "$(document).on(\"mobileinit\", function()\n" +
//            "{\n" +
//            "   $.mobile.ajaxEnabled = true;\n" +
//            "});\n" +
//            "</script>\n" +
//            "<script src=\"../default/jquery.mobile-1.4.5.min.js\"></script>\n" +
//            "\n" +
//            "\n" +
//            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
//            "</head>\n" +
//            "\n" +
//            "\n" +
//            "\n" +
//            "<body>\n" +
//            "<div data-role=\"page\" data-theme=\"a\" data-title=\"Untitled Page\" id=\"user_input\">\n" +
//            "<div data-role=\"header\" id=\"Header1\">\n" +
//            "<h1>添加成功！</h1>\n" +
//            "</div>\n" +
//            "\n" +
//            "<div class=\"ui-content\" role=\"main\">\n" +
//            "<label >添加成功！</label>\n" +
//            "</div>\n" +
//            "\n" +
//            "</div>\n" +
//            "</body>\n" +
//            "</html>";
    private UriResource uriResource;
    private Map<String, String> urlParams;
    private IHTTPSession session;

    @Override
    protected BufferedInputStream fileToInputStream(File fileOrdirectory) throws IOException {
        if ("exception.html".equals(fileOrdirectory.getName())) {
            throw new IOException("trigger something wrong");
        }
        return super.fileToInputStream(fileOrdirectory);
    }

    @Override
    public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        Map<String, String> params = session.getParms();

       // System.out.println("有请求_UserHandler");
        if (params.getOrDefault("doAction", "false").equalsIgnoreCase("false")) {
            return super.get(uriResource, urlParams, session);
        }
        //添加加密参与者
        if (session.getUri().contains("addEncryptionUser")) {
            // 如果不是点“确定”进这个页面的，回到填信息那个页面

            User user = new User();
            user.setName(String.valueOf(params.get("username")));
            user.setPassword(String.valueOf(params.get("password")));

            int processid = Integer.valueOf(params.get("processid"));
            EncryptProcess ep = AppNanolets.instance.getEncryptProcess(processid);
            try {
                user.setId(ep.getOptions().getTotal_users().size());
                ep.addUser(user);
                System.out.println("添加了一个用户");
                return newFixedLengthResponse(getStatus(), getMimeType(), String.valueOf(processid));
            } catch (Exception e) {
                e.printStackTrace();
                ep.setException(e);
            }

            //添加解密参与者
        } else if (session.getUri().contains("addDecryptionUser")) {
            // 如果不是点“确定”进这个页面的，回到填信息那个页面
            if (params.getOrDefault("doAction", "false").equalsIgnoreCase("true")) {


                int processid = Integer.valueOf(params.get("processid"));
                DecryptProcess dp = AppNanolets.instance.getDecryptProcess(processid);
                User user =dp.options.findUser(String.valueOf(params.get("username")));
                user.setPassword(String.valueOf(params.get("password")));
                try {
                    dp.addUser(user);
                    return newFixedLengthResponse(getStatus(), getMimeType(),  String.valueOf(processid));
                } catch (Exception e) {
                    e.printStackTrace();
                    dp.setException(e);
                }
            } else {
                return super.get(uriResource, urlParams, session);
            }
        }
        return super.get(uriResource, urlParams, session);
    }

    @Override
    public String getMimeType() {
        return "text/html";
    }

}