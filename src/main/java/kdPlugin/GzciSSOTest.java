package kdPlugin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GzciSSOTest {


    /**
     * 该方法是用户没有登录的时候插件需要转移到正确的登录地址
     */


    /**
     * 该方法实现第三发插件认证及认证结果的返回
     */

    public static void main(String[] args){
//    Logger logger = Logger.getLogger(GzciSSOTest.class);
        System.out.println("时间:"+new Date());
        String GET_TOKEN = "https://cloud-id.gzci.net/oauth2.0/token";
        String GET_USERINFO ="https://cloud-id.gzci.net/oauth2.0/user-info";
        String CLIENT_ID="5e6ae050-29cc-2015-8a29-0148090aa296";
        String CLIENT_SECRET="a44d1490-ccdc-7068-b9bb-d8c0289ed83a";
        String REDIRECT_URI = "http://172.18.27.133:8022/ierp/login.html";
        System.out.println("GzciSSO getTrdSSOAuth");
//        UserAuthResult result = new UserAuthResult();
//        result.setSucess(false);
        String code = "OC-5-180535502-NeKdQtAHEJpFW9TyDXaXHJVSsdMPqEPMMTy";
        System.out.println("GzciSSO getTrdSSOAuth code="+code);
        Map<String, String> params = new HashMap<String, String>();
        params.put("grant_type","authorization_code");
        params.put("code",code);
        params.put("redirect_uri",REDIRECT_URI);
//        params.put("Content-Type","application/x-www-form-urlencoded");
        try {
//            String authorization = "Basic "+Base64.getEncoder().encodeToString((CLIENT_ID+":"+CLIENT_SECRET).getBytes("utf8"));
            String authorization = "Basic NWU2YWUwNTAtMjljYy0yMDE1LThhMjktMDE0ODA5MGFhMjk2OmE0NGQxNDkwLWNjZGMtNzA2OC1iOWJiLWQ4YzAyODllZDgzYQ==";
            System.out.println("GzciSSO getTrdSSOAuth authorization="+authorization);
            params.put("Authorization",authorization);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        String getTokenRelult=null;
        String access_token = null;
        try {
            getTokenRelult = HttpClientUtils.sendPostByUrlEncoder(GET_TOKEN,params,params);
            System.out.println("GzciSSO getTrdSSOAuth getTokenRelult="+getTokenRelult);
            JSONObject jSONObject =  JSON.parseObject(getTokenRelult);
            String status = jSONObject.getString("status");
            if(status!=null && status.equals("401")){
                System.out.println("GzciSSO getTrdSSOAuth Unauthorized");
                return;
            }else {
                String rspCode = jSONObject.getString("code");
                if(rspCode!=null &&rspCode.equals("30072")){
                    System.out.println("GzciSSO getTrdSSOAuth code 过期");
                    return;
                }else {
                    access_token = jSONObject.getString("access_token");
                }
            }
            System.out.println("GzciSSO getTrdSSOAuth access_token="+access_token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, String> params2 = new HashMap<String, String>();
        params2.put("Authorization","Bearer "+access_token);
        String getUserInfoResult = null;
        try {
            System.out.println("时间2:"+new Date());
            getUserInfoResult = HttpClientUtils.sendPostByUrlEncoder(GET_USERINFO,null,params2);
            System.out.println("GzciSSO getTrdSSOAuth getUserInfoResult="+getUserInfoResult);
            JSONObject getUserInfoResultJsonObject =  JSON.parseObject(getUserInfoResult);
            String phone = getUserInfoResultJsonObject.getString("phone");
            System.out.println("GzciSSO getTrdSSOAuth phone="+phone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}