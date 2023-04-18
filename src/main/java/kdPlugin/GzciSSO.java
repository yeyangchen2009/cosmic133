package kdPlugin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kd.bos.login.thirdauth.ThirdSSOAuthHandler;
import javax.servlet.http. HttpServletRequest;
import javax.servlet.http. HttpServletResponse;
import kd.bos.login.thirdauth.UserAuthResult;
import kd.bos.login.thirdauth.UserProperType;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class GzciSSO implements ThirdSSOAuthHandler {
    Logger logger = Logger.getLogger(this.getClass());
    String GET_TOKEN = "https://cloud-id.gzci.net/oauth2.0/token";
    String GET_USERINFO ="https://cloud-id.gzci.net/oauth2.0/user-info";
    String CLIENT_ID="5e6ae050-29cc-2015-8a29-0148090aa296";
    String CLIENT_SECRET="a44d1490-ccdc-7068-b9bb-d8c0289ed83a";
    String REDIRECT_URI = "http://172.18.27.133:8022/ierp";
    String INDEX_URI = "http://172.18.27.133:8022/ierp/index.html";

    String LOGIN_URI = "http://172.18.27.133:8022/ierp/login.html";

    // String SSO_URI = "https://cloud-id.gzci.net/oauth2.0/authorize?client_id=5e6ae050-29cc-2015-8a29-0148090aa296&redirect_uri=http%3A%2F%2F192.168.139.52%3A8022%2Fierp&response_type=code";
    String SSO_URI = "https://cloud-id.gzci.net/oauth2.0/authorize?client_id=5e6ae050-29cc-2015-8a29-0148090aa296&redirect_uri=http%3A%2F%2F172.18.27.133%3A8022%2Fierp&response_type=code";
    /**
     * 该方法是用户没有登录的时候插件需要转移到正确的登录地址
     */
    public void callTrdSSOLogin(HttpServletRequest request, HttpServletResponse response, String backUrl) {
        logger.info("GzciSSO callTrdSSOLogin 20230404");
        this.logger.info("GzciSSO callTrdSSOLogin getServletPath="+request.getServletPath());
        this.logger.info("GzciSSO callTrdSSOLogin getRequestURL="+  request.getRequestURL());
        logger.info("GzciSSO callTrdSSOLogin backUrl="+backUrl);

        if (request.getRequestURI().contains("logout.do")){
            try {
                //
                response.sendRedirect(LOGIN_URI);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String code = request.getParameter("code");
        if(code!=null){
            logger.info("GzciSSO callTrdSSOLogin jump getTrdSSOAuth start");
            UserAuthResult userAuthResult = getTrdSSOAuth(request,response);
            logger.info("GzciSSO callTrdSSOLogin jump getTrdSSOAuth end");
            if(userAuthResult.isSucess()) {
                logger.info("GzciSSO callTrdSSOLogin jump getTrdSSOAuth result sucess");
                try {
                    logger.info("GzciSSO callTrdSSOLogin jump INDEX_URI=" + INDEX_URI);
                    response.sendRedirect(INDEX_URI);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                logger.info("GzciSSO callTrdSSOLogin jump getTrdSSOAuth result fail");
            }
            return;
        }
        //用户需要登录的地址
        try {
            logger.info("GzciSSO callTrdSSOLogin SSO_URI="+SSO_URI);
            response.sendRedirect(SSO_URI);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 该方法实现第三发插件认证及认证结果的返回
     */
    @Override
    public UserAuthResult getTrdSSOAuth(HttpServletRequest request, HttpServletResponse response) {
        // TODO Auto-generated method stub
        logger.info("GzciSSO getTrdSSOAuth 20230404");
        this.logger.info("GzciSSO getTrdSSOAuth getServletPath="+request.getServletPath());
        this.logger.info("GzciSSO getTrdSSOAuth getRequestURL="+  request.getRequestURL());
        Enumeration enu=request.getParameterNames();
        while(enu.hasMoreElements()){
            String paraName=(String)enu.nextElement();
            System.out.println(paraName+": "+request.getParameter(paraName));
            logger.info("GzciSSO getTrdSSOAuth getParameterNames");
            logger.info("GzciSSO getTrdSSOAuth paraName="+paraName+":"+request.getParameter(paraName));
        }
        //formId:pc_main_console
        String formId = request.getParameter("formId");
        if(formId!=null && "pc_main_console".equals(formId)){
            logger.info("GzciSSO getTrdSSOAuth already login,return null");
            return null;
        }
        UserAuthResult result = new UserAuthResult();
        result.setSucess(false);
        String code = request.getParameter("code");
        if(code == null){
            logger.info("GzciSSO getTrdSSOAuth code null ,return false");
            return result;
        }

        logger.info("GzciSSO getTrdSSOAuth code="+code);
        Map<String, String> params = new HashMap<String, String>();
        params.put("grant_type","authorization_code");
        params.put("code",code);
        params.put("redirect_uri",REDIRECT_URI);
        try {
            params.put("Authorization","Basic "+ Base64.getEncoder().encodeToString((CLIENT_ID+":"+CLIENT_SECRET).getBytes("utf8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String getTokenRelult=null;
        String access_token = null;
        try {
            getTokenRelult = HttpClientUtils.sendPostByUrlEncoder(GET_TOKEN,params,params);
            logger.info("GzciSSO getTrdSSOAuth getTokenRelult="+getTokenRelult);
            JSONObject jSONObject =  JSON.parseObject(getTokenRelult);
            String status = jSONObject.getString("status");
            if(status!=null && status.equals("401")){
                logger.info("GzciSSO getTrdSSOAuth Unauthorized");
                result.setErrDesc("Unauthorized");
                result.setSucess(false);
            }else {
                String rspCode = jSONObject.getString("code");
                if(rspCode!=null &&rspCode.equals("30072")){
                    logger.info("GzciSSO getTrdSSOAuth code 过期");
                    result.setErrDesc("grant 过期");
                    result.setSucess(false);
                }else {
                    access_token = jSONObject.getString("access_token");
                    logger.info("GzciSSO getTrdSSOAuth access_token="+access_token);
                }
            }
        } catch (Exception e) {
            logger.info("GzciSSO getTrdSSOAuth exception="+e.getMessage());
            throw new RuntimeException(e);
        }

        if(access_token!=null) {
            Map<String, String> params2 = new HashMap<String, String>();
            params2.put("Authorization", "Bearer " + access_token);
            String getUserInfoResult = null;
            try {
                logger.info("GzciSSO getTrdSSOAuth 时间2:" + new Date());
                getUserInfoResult = HttpClientUtils.sendPostByUrlEncoder(GET_USERINFO, params2, params2);
                logger.info("GzciSSO getTrdSSOAuth getUserInfoResult=" + getUserInfoResult);
                JSONObject getUserInfoResultJsonObject = JSON.parseObject(getUserInfoResult);
                String phone = getUserInfoResultJsonObject.getString("phone");
                logger.info("GzciSSO getTrdSSOAuth phone=" + phone);

                if (phone != null) {
                    //当前返回类型手机，用户名，email，工号
                    result.setUserType(UserProperType.Mobile);
                    result.setUser(phone);
                    result.setSucess(true);
                }
            } catch (Exception e) {
                logger.info("GzciSSO getTrdSSOAuth exception=" + e.getMessage());
                throw new RuntimeException(e);
            }
        }else{
            logger.info("GzciSSO getTrdSSOAuth access_token 获取失败");
        }
        return result;
    }


    /**
     * SSO 登出逻辑,退出成功,返回true，不需要在sso插件中处理可以不用实现
     */
    public boolean processLogoutLogic(HttpServletRequest servletRequest){
        this.logger.info("GzciSSO processLogoutLogic");
        this.logger.info("GzciSSO processLogoutLogic getServletPath="+servletRequest.getServletPath());
        this.logger.info("GzciSSO processLogoutLogic getRequestURL="+  servletRequest.getRequestURL());

        Enumeration enu=servletRequest.getParameterNames();
        while(enu.hasMoreElements()){
            String paraName=(String)enu.nextElement();
            System.out.println(paraName+": "+servletRequest.getParameter(paraName));
            logger.info("GzciSSO processLogoutLogic getParameterNames");
            logger.info("GzciSSO processLogoutLogic paraName="+paraName+":"+servletRequest.getParameter(paraName));
        }
        return true;
    }

}

