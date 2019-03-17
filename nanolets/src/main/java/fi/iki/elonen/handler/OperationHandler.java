package fi.iki.elonen.handler;

import fi.iki.elonen.AppNanolets;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.crypto.CryptoOptions;
import fi.iki.elonen.crypto.DecryptProcess;
import fi.iki.elonen.crypto.EncryptProcess;
import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import org.apache.commons.fileupload.FileItem;

import javax.crypto.Cipher;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class OperationHandler extends RouterNanoHTTPD.StaticPageHandler {
    String a ="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "<head>\n" +
            "        <meta charset=\"utf-8\" />\n" +
            "        <title>showrul</title>\n" +
            "        <!--这里要一个祭品，第一个css加载不出来！-->\n" +
            "        <link href=\"../default/a.css\" rel=\"stylesheet\"\n" +
            "        />\n" +
            "        <link href=\"../default/jquery.mobile.theme-1.4.5.css\" rel=\"stylesheet\"\n" +
            "        />\n" +
            "        <link href=\"../default/jquery.mobile.icons-1.4.5.min.css\" rel=\"stylesheet\"\n" +
            "        />\n" +
            "        <link href=\"../default/jquery.mobile.structure-1.4.5.min.css\" rel=\"stylesheet\"\n" +
            "        />\n" +
            "        <link href=\"setup.css\" rel=\"stylesheet\" />\n" +
            "        <script src=\"../default/jquery-1.12.4.min.js\">\n" +
            "        </script>\n" +
            "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
            "         <script src=\"../default/jquery.mobile-1.4.5.min.js\"></script>\n" +
            "        <script>\n" +
            "            $(document).on(\"mobileinit\",\n" +
            "            function() {\n" +
            "                $.mobile.ajaxEnabled = true;\n" +
            "            });\n" +
            "        \n" +
            "       \n" +
            "        </script>\n" +
            "    </head>\n" +
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
    public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        Map<String, String> params = session.getParms();
        System.out.println("有请求_OperationHandler");

        if (params.getOrDefault("doAction", "false").equalsIgnoreCase("false")) {
            return super.get(uriResource, urlParams, session);
        }
        if (session.getUri().contains("setup")) {

            CryptoOptions options = new CryptoOptions(Cipher.ENCRYPT_MODE);
            options.setCapsulename(String.valueOf(params.get("capsulename")));
            options.setTotalNum(Integer.valueOf(params.get("totalnum")));
            options.setLeastNum(Integer.valueOf(params.get("leastnum")));

            EncryptProcess ep = new EncryptProcess(options);
            AppNanolets.instance.addEncryptProcess(ep);

            // 注意，一定要先加入再获取id!
            return url_page("../user/addEncryptionUser.html?processid=" + ep.getProcessid());

            //解密的过程在upload里面创建，因为它一开始就上传
        } else if (session.getUri().contains("unpack")) {
// 特别地，解密的过程id由客户端创建
            return url_page("../user/addDecryptionUser.html?processid=" + params.get("processid"));
        }

        return super.get(uriResource, urlParams, session);
    }



    private Response url_page(String url) {
        return newFixedLengthResponse(getStatus(), getMimeType(), a + url + b);
    }

    @Override
    public String getMimeType() {
        return "text/html";
    }
}
