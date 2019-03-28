package fi.iki.elonen.handler;

import com.alibaba.fastjson.JSONObject;
import fi.iki.elonen.AppNanolets;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.crypto.CryptoOptions.User;
import fi.iki.elonen.crypto.DecryptProcess;
import fi.iki.elonen.crypto.EncryptProcess;
import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatusHandler  extends RouterNanoHTTPD.DefaultStreamHandler {
AppNanolets instance;
    public StatusHandler(){
        this.instance=AppNanolets.instance;
    }
    @Override
    public NanoHTTPD.Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        Map<String, String> params=session.getParms();

        int processid=Integer.valueOf(params.get("processid"));
        if(params.get("action").equalsIgnoreCase("getEncryptProcessStatus")){
            JSONObject json=new JSONObject();

            EncryptProcess ep=instance.getEncryptProcess(processid);
            json.put("capsulename",ep.getOptions().getCapsulename());
            json.put("totalNum",ep.getOptions().getTotalNum());
            json.put("leastNum",ep.getOptions().getLeastNum());
            json.put("completedNum",ep.getOptions().getTotal_users().size());
            json.put("completedUsers",getUsersNames(ep.getOptions().getTotal_users()));
            json.put("canDownload",ep.getOptions().canDownload);

            return NanoHTTPD.newChunkedResponse(getStatus(),getMimeType(),toInputStream(json.toJSONString()));
        }else{
            JSONObject json=new JSONObject();

            DecryptProcess dp=instance.getDecryptProcess(processid);

            List<User> leftUsers=new ArrayList<>();
            List<User> totalUsers=dp.getOptions().getTotal_users();
            List<User> decryptingUsers=dp.getOptions().getDecrypting_users();
            for(User user:totalUsers){
                if(!decryptingUsers.contains(user)){
                    leftUsers.add(user);
                }
            }
            System.out.println("剩下没输密码的解密者："+getUsersNames(leftUsers));
            json.put("capsulename",dp.getOptions().getCapsulename());
            json.put("totalNum",dp.getOptions().getTotalNum());
            json.put("leastNum",dp.getOptions().getLeastNum());
            json.put("completedNum",dp.getOptions().getDecrypting_users().size());
            json.put("completedUsers",getUsersNames(decryptingUsers));
            json.put("totalUserNames",getUsersNames(totalUsers));
            json.put("leftUsersNames",getUsersNames(leftUsers));
            json.put("canDownload",dp.getOptions().canDownload);
            return NanoHTTPD.newChunkedResponse(getStatus(),getMimeType(),toInputStream(json.toJSONString()));
        }











      //  return NanoHTTPD.newChunkedResponse(getStatus(), getMimeType(), getData());
    }

    public List<String> getUsersNames(List<User> users){
        List<String> names=new ArrayList<>();
        for(User u:users){
            names.add(u.getName());
        }
        return names;
    }

    public InputStream toInputStream(String data){
        return new ByteArrayInputStream(data.getBytes());
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
        return new ByteArrayInputStream("faild".getBytes());
    }
}

