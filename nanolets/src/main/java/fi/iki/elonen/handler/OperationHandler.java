package fi.iki.elonen.handler;

import fi.iki.elonen.AppNanolets;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.crypto.CryptoOptions;
import fi.iki.elonen.crypto.EncryptProcess;
import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class OperationHandler extends RouterNanoHTTPD.StaticPageHandler {
    @Override
    public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        Map<String, String> params = session.getParms();
        System.out.println("有请求_OperationHandler");

        if (session.getUri().contains("setup")) {
            if (params.getOrDefault("doAction", "false").equalsIgnoreCase("true")) {
                CryptoOptions options = new CryptoOptions();
                options.setCapsulename(String.valueOf(params.get("capsulename")));
                options.setTotalNum(Integer.valueOf(params.get("totalnum")));
                options.setLeastNum(Integer.valueOf(params.get("leastnum")));

                EncryptProcess ep = new EncryptProcess(options);
                AppNanolets.instance.addEncryptProcess(ep);

                // 注意，一定要先加入再获取id!
                return url_page("../user/addEncryptionUser.html?process="+ep.getProcessid());
            }
        } else {
            return super.get(uriResource, urlParams, session);
        }

        return super.get(uriResource, urlParams, session);
    }


    private Response url_page(String url) {
        return newFixedLengthResponse(getStatus(), getMimeType(), a + url + b);
    }

    String a = "<!doctype html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<meta charset=\"utf-8\">\n" +
            "<title>showrul</title>\n" +
            "\n" +
            "<link href=\"../default/jquery.mobile.theme-1.4.5.css\" rel=\"stylesheet\">\n" +
            "<link href=\"../default/jquery.mobile.icons-1.4.5.min.css\" rel=\"stylesheet\">\n" +
            "<link href=\"../default/jquery.mobile.structure-1.4.5.min.css\" rel=\"stylesheet\">\n" +
            "\n" +
            "<link href=\"setup.css\" rel=\"stylesheet\">\n" +
            "<script src=\"../default/jquery-1.12.4.min.js\"></script>\n" +
            "<script>\n" +
            "$(document).on(\"mobileinit\", function()\n" +
            "{\n" +
            "   $.mobile.ajaxEnabled = true;\n" +
            "});\n" +
            "</script>\n" +
            "<script src=\"../default/jquery.mobile-1.4.5.min.js\"></script>\n" +
            "\n" +
            "\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
            "</head>\n" +
            "\n" +
            "\n" +
            "\n" +
            "<body>\n" +
            "<div data-role=\"page\" data-theme=\"a\" data-title=\"Untitled Page\" id=\"user_input\">\n" +
            "<div data-role=\"header\" id=\"Header1\">\n" +
            "<h1>showrul</h1>\n" +
            "</div>\n" +
            "\n" +
            "<div class=\"ui-content\" role=\"main\">\n" +
            "<a href=\"";
    String b = "\">点我</a>\n" +
            "</div>\n" +
            "\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>";

    @Override
    public String getMimeType() {
        return "text/html";
    }
}
