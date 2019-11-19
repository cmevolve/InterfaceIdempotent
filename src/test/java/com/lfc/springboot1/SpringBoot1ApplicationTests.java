package com.lfc.springboot1;

import com.lfc.springboot1.annotation.InterfaceIdempotence;
import com.lfc.springboot1.util.JedisUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
class SpringBoot1ApplicationTests {
    @Autowired
    private JedisUtil jedisUtil;

    @Test
    public void contextLoads() throws Exception {
        String casurl = "http://hxtest.huahaibaoxian.com:8000/cas/login";
        String serverUrl = "http://hxtest.huahaibaoxian.com:8000/pcisv7/j_spring_security_check";
        String username = "101001000002";
        String password = "111";
        String ticket = getTicket(casurl,serverUrl,username,password);
        System.out.println(ticket);
        genLogin(username, password, serverUrl, casurl);
    }

    @Test
    public void myTest(){
        System.out.println("======加锁==========");
        System.out.println(jedisUtil.tryGetDistributedLock("sf","sfa",100000));
        System.out.println(jedisUtil.get("sf"));
        System.out.println("======重复加锁==========");
        System.out.println(jedisUtil.tryGetDistributedLock("sf","zcxv",200000));
        System.out.println("======非正确释放锁==========");
        System.out.println(jedisUtil.releaseDistributedLock("sf","zcxv"));
        System.out.println("======正确释放==========");
        System.out.println(jedisUtil.releaseDistributedLock("sf","sfa"));
        System.out.println("======重复释放==========");
        System.out.println(jedisUtil.releaseDistributedLock("sf","sfa"));
    }



    private static String doCasLoginRequest(HttpClient httpclient, String url)
            throws IOException {
        String result = "";
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        BufferedReader rd = new BufferedReader(new InputStreamReader(entity
                .getContent(), "UTF-8"));
        String tempLine = rd.readLine();
        StringBuilder sb = new StringBuilder(tempLine);

        String s = "<input type=\"hidden\" name=\"lt\" value=\"";
        while (tempLine != null) {
            int index = tempLine.indexOf(s);
            if (index != -1) {
                String s1 = tempLine.substring(index + s.length());
                int index1 = s1.indexOf("\"");
                if (index1 != -1)
                    result = s1.substring(0, index1);
            }

            tempLine = rd.readLine();
            sb.append(tempLine);
        }
        if (entity != null) {
            entity.consumeContent();
        }
        System.out.println("第二次请求："+sb.toString());
        return result;
    }
    public String getTicket(String casurl,
                            String service, String username, String password) throws Exception {
        String lastUrl = "http://hxtest.huahaibaoxian.com:8000/pcisv7/jsp/payseemoney/vhl_fee_card_list.jsp?dptCde=10&mopCde=015010001";
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore).build();
        String loction = "";
        try {
            HttpGet httpget = new HttpGet(lastUrl);
            CloseableHttpResponse response1 = httpclient.execute(httpget);
//
            HttpEntity entity = response1.getEntity();

            System.out.println("第一次请求： " + response1.getStatusLine());
            String serviceurl = URLEncoder.encode(service, "utf-8");

            // String service
            // =URLEncoder.encode("http://hxtest.huahaibaoxian.com:8000/pcis/j_spring_security_check"
            // ,"utf-8");
            EntityUtils.consume(entity);
            String url=casurl + "?service="+ serviceurl;
            System.out.println(url);
            //获取lt，get请求获取页面元素
            String ticket = doCasLoginRequest(httpclient, url);

            HttpUriRequest login = RequestBuilder.post()
                    .setUri(new URI(casurl)).addParameter("username", username)
                    .addParameter("password", password).addParameter("lt",
                            ticket).addParameter("_eventId", "submit")
                    .addParameter("submit", "登录").addParameter("execution",
                            "e2s1").build();
            CloseableHttpResponse response2 = httpclient.execute(login);

            HttpEntity entity2 = response2.getEntity();
            String body = EntityUtils.toString(response2.getEntity());
            System.out.println("第三次请求："+body);
            // Header locationHeader = response2.getFirstHeader("location");
            //得到ticket
            loction = response2.getFirstHeader("Location").getValue();
            System.out.println("locatin:" + loction);
            EntityUtils.consume(entity2);
            System.out.println("Initial set of cookies:");
            HttpGet httpget1 = new HttpGet(loction);
            CloseableHttpResponse response3 = httpclient.execute(httpget1);
            System.out.println("local访问"+EntityUtils.toString(response3.getEntity()));
            List<Cookie> cookies = cookieStore.getCookies();


            HttpGet httpget2 = new HttpGet(lastUrl);
            CloseableHttpResponse response4 = httpclient.execute(httpget2);
            System.out.println("最后访问："+EntityUtils.toString(response4.getEntity()));
//            List<Cookie> cookies = cookieStore.getCookies();
//            if (cookies.isEmpty()) {
//                System.out.println("None");
//            } else {
//                for (int i = 0; i < cookies.size(); i++) {
//
//                    res.addCookie(convertToServletCookie(cookies.get(i)));
//					System.out.println("："+cookies.get(i).getName());
//					if("JSESSIONID".equals(cookies.get(i).getName())){
//						return cookies.get(i).getValue();
//					}
//                }


//            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return loction;

    }
    public static CASResult genLogin(String username, String password, String redirectUrl,String casurl) throws Exception {
        // 创建客户端
        CookieStore httpCookieStore = new BasicCookieStore();
        CloseableHttpClient client = createHttpClientWithNoSsl(httpCookieStore);

        /* 第一次请求[GET] 拉取流水号信息 */
        HttpGet request = new HttpGet(casurl+"?service=" + redirectUrl);
        HttpResponse response = client.execute(request);
        StringBuilder executionVal =new StringBuilder();
        StringBuilder ltVal =new StringBuilder();
        readResponse(response,ltVal,executionVal);

        String _eventId = "submit";
        String submit = "登录";

        /* 第二次请求[POST] 发送表单验证信息 */
        HttpPost request2 = new HttpPost(casurl);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("execution", executionVal.toString()));
        params.add(new BasicNameValuePair("lt", ltVal.toString()));
        params.add(new BasicNameValuePair("_eventId", _eventId));
        params.add(new BasicNameValuePair("submit", submit));
        request2.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response2 = client.execute(request2);

        Header headerSetCookie = response2.getFirstHeader("Set-Cookie");
        String TGC = headerSetCookie == null ? null : headerSetCookie.getValue().substring(4, headerSetCookie.getValue().indexOf(";")); // TGC
        Header headerLocation = response2.getFirstHeader("Location");
        String location = headerLocation == null ? null : headerLocation.getValue();
        System.out.println(location);
        /* 第三次请求[GET]，前往 CAS 客户端进行验证获取会话 */
        HttpGet request3 = new HttpGet(location);
        HttpResponse response3 = client.execute(request3);

        CASResult casResult = new CASResult();
        List<Cookie> cookies = httpCookieStore.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie != null && cookie.getName().equals("TGC")) {
                casResult.setTGC(cookie.getValue());
                continue;
            }
            if (cookie != null && cookie.getName().equals("JSESSIONID")) {
                casResult.setJSESSIONID(cookie.getValue());
                continue;
            }
        }
        // set Uri (scheme, host, path, query..)
        casResult.setUri(request3.getURI());
        /* 第四次请求[GET]，获取目标页面内容 */
        /* 该获取移交由用户自行操作，不在此做多余请求 */

        return casResult;
    }
    /* 读取 response body 内容为字符串 */
    private static void readResponse(HttpResponse response,StringBuilder ltVal,StringBuilder executionVal) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        String lt = "<input type=\"hidden\" name=\"lt\" value=\"";
        String execution = "<input type=\"hidden\" name=\"execution\" value=\"";
        String line;
        while ((line = in.readLine()) != null) {
            int index = line.indexOf(lt);
            if (index != -1) {
                String s1 = line.substring(index + lt.length());
                int index1 = s1.indexOf("\"");
                if (index1 != -1){
                    ltVal.append(s1.substring(0, index1));
                }
            }
            int index2 = line.indexOf(execution);
            if (index2 != -1) {
                String s2 = line.substring(index2 + execution.length());
                int index1 = s2.indexOf("\"");
                if (index1 != -1){
                    executionVal.append(s2.substring(0, index1));
                }
            }
        }
    }
    /**
     * 创建模拟客户端（针对 https 客户端禁用 SSL 验证）
     *
     * @param cookieStore 缓存的 Cookies 信息
     * @return
     * @throws Exception
     */
    private static CloseableHttpClient createHttpClientWithNoSsl(CookieStore cookieStore) throws Exception {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // don't check
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // don't check
                    }
                }
        };

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAllCerts, null);
        LayeredConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(ctx);
        return HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .setDefaultCookieStore(cookieStore == null ? new BasicCookieStore() : cookieStore)
                .build();
    }


    /**
     * 最终返回结果集
     */
    public static class CASResult {
        /* 认证通过则返回 TGC，否则为空 */
        private String TGC;
        private String JSESSIONID; // 会话 ID
        private URI uri; // 包含 (scheme, host, path, query..)

        public String getTGC() {
            return TGC;
        }

        public void setTGC(String TGC) {
            this.TGC = TGC;
        }

        public String getJSESSIONID() {
            return JSESSIONID;
        }

        public void setJSESSIONID(String JSESSIONID) {
            this.JSESSIONID = JSESSIONID;
        }

        public URI getUri() {
            return uri;
        }

        public void setUri(URI uri) {
            this.uri = uri;
        }
    }
}
