package fi.iki.elonen;


import fi.iki.elonen.crypto.DecryptProcess;
import fi.iki.elonen.crypto.EncryptProcess;
import fi.iki.elonen.handler.DownloadHandler;
import fi.iki.elonen.handler.OperationHandler;
import fi.iki.elonen.handler.UploadHandler;
import fi.iki.elonen.handler.UserHandler;
import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// 只是一个中转类罢了，看RouterNanoHTTPD
public class AppNanolets extends RouterNanoHTTPD {

    private static final int PORT = 9090;
    private Map<Integer, EncryptProcess> encryptings = new HashMap<>();
    private Map<Integer, DecryptProcess> decryptings = new HashMap<>();
    public static AppNanolets instance;

    /**
     * Create the server instance
     */
    public AppNanolets() throws IOException {
        super(PORT);
        addMappings();
        instance = this;
        System.out.println("\nRunning! Point your browers to http://localhost:" + PORT + "/ \n");
    }

    /**
     * Add the routes Every route is an absolute path Parameters starts with ":"
     * Handler class should implement @UriResponder interface If the handler not
     * implement UriResponder interface - toString() is used
     */
    @Override
    public void addMappings() {
        Logger.getLogger(NanoHTTPD.class.getName()).setLevel(Level.ALL);
        System.out.println(Logger.getLogger(NanoHTTPD.class.getName()).getLevel());
        // Logger.getLogger(NanoHTTPD.class.getName()).log(Level.CONFIG,"123123123");
        super.addMappings();
        addRoute("/upload", UploadHandler.class);
        addRoute("/operation/(.)+", OperationHandler.class, new File(
                "C:\\Users\\QinHuoBin\\Desktop\\网页设计\\OperationHandler\\").getAbsoluteFile());
        addRoute("/download/(.)+", DownloadHandler.class);
         addRoute("/user/(.)+", UserHandler.class,new File(
                 "C:\\Users\\QinHuoBin\\Desktop\\网页设计\\UserHandler\\").getAbsoluteFile());


         addRoute("/default/(.)+",StaticPageHandler.class,new File(
                 "C:\\Users\\QinHuoBin\\Desktop\\网页设计\\default\\").getAbsoluteFile());
//        addRoute("/user", UserHandler.class); // add it twice to execute the
//                                              // priority == priority case
//        addRoute("/user/help", GeneralHandler.class);
//        addRoute("/user/:id", UserHandler.class);
//        addRoute("/general/:param1/:param2", GeneralHandler.class);
//        addRoute("/photos/:customer_id/:photo_id", null);
//        addRoute("/test", String.class);
//        addRoute("/interface", UriResponder.class); // this will cause an error
//                                                    // when called
//        addRoute("/toBeDeleted", String.class);
//        removeRoute("/toBeDeleted");
//        addRoute("/stream", StreamUrl.class);
//        addRoute("/browse/(.)+", StaticPageTestHandler.class, new File("src/test/resources").getAbsoluteFile());
    }

    public static void main(String[] args) throws Exception {
//        for (Provider provider : Security.getProviders()){
//            System.out.println("Provider: " + provider.getName());
//            for (Provider.Service service : provider.getServices()){
//                System.out.println("  Algorithm: " + service.getAlgorithm());
//            }
//            System.out.println("\n");
//        }
        ServerRunner.run(AppNanolets.class);
    }

    public EncryptProcess getEncryptProcess(int processid){
        return encryptings.get(processid);
    }

    public DecryptProcess getDecryptProcess(int processid){
        return decryptings.get(processid);
    }

    public void addEncryptProcess(EncryptProcess ep) {
        ep.setProcessid(encryptings.size());
        encryptings.put(encryptings.size(), ep);
        System.out.println("添加了加密过程"+encryptings);
    }

    public void addDecryptProcess(DecryptProcess dp) {
        dp.setProcessid(encryptings.size());
        decryptings.put(decryptings.size(), dp);
        System.out.println("添加了解密过程"+encryptings);
    }
}
