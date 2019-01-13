package com.chaindd.utils;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import okhttp3.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * OKhttpClient工具类,接收参数,请求url,解析并返回数据
 *
 * @Author: xinyueyan
 * @Date: 9/11/2018 11:51 AM
 */
public class OkHttpClientUtil {

    protected static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS).connectTimeout(30,TimeUnit.SECONDS).build();

    /**
     * 仅通过url获取数据,无需参数
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String getData(String url) throws IOException {
        //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("114.112.70.150", 43887));
        //OkHttpClient cli = new OkHttpClient().newBuilder().proxy(proxy).build();
        Request request = new Request.Builder().url(url).build();
                //.header("device","win10")
                //.header("app_version", "1")
        //log.info("url==========="+url);
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            //log.info("====OkHttpClientUtil.getData======error");
            return null;
            //throw new IOException("Unexpected code " + response);
        }
    }

    /**
     * 构建get请求获取数据,需要参数(httpHeader信息/其他参数信息)
     *
     * @param url 请求地址
     * @return 返回获取的数据
     * @throws IOException
     */
    public static Object getDataWithParamsAndHeaders(String url, String headersAndParams) throws IOException {
        HashMap<String, Object> hp = gson.fromJson(headersAndParams, HashMap.class);
        LinkedTreeMap headers = (LinkedTreeMap) hp.get("headers");
        LinkedTreeMap params = (LinkedTreeMap) hp.get("params");
        //log.info(headers.toString());
        if (headers == null) {
            return null;
        }
        //处理paramsJson,拼接url
        if (params == null) {
            url = url;
        } else {
            String paramsStr = "";
            Set<String> keys = params.keySet();
            for (String key : keys) {
                paramsStr += ("&" + key + "=" + params.get(key));
            }
            paramsStr = paramsStr.substring(1);
            url = url + "?" + paramsStr;
        }
        //log.info(url);
        //处理请求头信息
        OkHttpClient client = new OkHttpClient();
        Request.Builder reqBuilder = new Request.Builder().url(url);
         //处理app-key:Double-->Integer
        headers.put("app-key",((Double)headers.get("app-key")).intValue());
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            reqBuilder = reqBuilder.addHeader(key, headers.get(key).toString());
        }
        Request request = reqBuilder.build();
        Response response = client.newCall(request).execute();
        if(!response.isSuccessful()){
            return null;
        }
        //处理并返回数据
        //return handleWithResponse(response);
        return null;
    }

    /**
     * 构建post请求获取数据
     * @param url 接口路径
     * @param headersAndParams
     * @return
     */
    public static Object postWithParamsAndHeaders(String url, String headersAndParams)throws IOException {
        HashMap<String, Object> hp = gson.fromJson(headersAndParams, HashMap.class);
        LinkedTreeMap headers = (LinkedTreeMap) hp.get("headers");
        LinkedTreeMap params = (LinkedTreeMap) hp.get("params");
        //log.info(params.toString());
        if (headers == null) {
            return null;
        }
        String paramsJson = gson.toJson(params);
        //log.info("====post提交参数===="+paramsJson);
        //构建请求
        RequestBody body = RequestBody.create(JSON, paramsJson);
        Request.Builder reqBuilder = new Request.Builder().url(url);
        //处理app-key:Double-->Integer
        headers.put("app-key",((Double)headers.get("app-key")).intValue());
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            reqBuilder = reqBuilder.addHeader(key, headers.get(key).toString());
        }
        Request request = reqBuilder.post(body).build();
        Response response = client.newCall(request).execute();
        if(!response.isSuccessful()){
            return null;
        }
        //return handleWithResponse(response);
        return null;
    }

    /**
     * 处理response数据
     * @param response
     * @return
     */
/*    private static Object handleWithResponse(Response response){
        ResponseBody body = response.body();
        ResponseOfApi responseOfApi = new ResponseOfApi();
        try {
            //转码
            String res = asciiToNative(response.body().string());
            //解析数据并封装
            HashMap<String, Object> fromJson = gson.fromJson(res, HashMap.class);
            Object data = fromJson.get("data");
            if (data == null) {
                return "查无数据";
            }
            responseOfApi.setResults(data);
            LinkedTreeMap<String, Object> cursor = (LinkedTreeMap) fromJson.get("cursor");
            if (cursor == null) {
                return responseOfApi;
            }
            responseOfApi.setTotal(((Double) cursor.get("total")).intValue());
            responseOfApi.setLimit(((Double) cursor.get("limit")).intValue());
            responseOfApi.setOffset(((Double) cursor.get("offset")).intValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseOfApi;
    }*/

    /**
     * OkHttpClient获取的数据是Unicode编码,转换为中文
     *
     * @param asciicode 待转码字符串
     * @return 转码后字符串
     */
    private static String asciiToNative(String asciicode) {
        String[] asciis = asciicode.split("\\\\u");
        String nativeValue = asciis[0];
        try {
            for (int i = 1; i < asciis.length; i++) {
                String code = asciis[i];
                nativeValue += (char) Integer.parseInt(code.substring(0, 4), 16);
                if (code.length() > 4) {
                    nativeValue += code.substring(4, code.length());
                }
            }
        } catch (NumberFormatException e) {
            return asciicode;
        }
        return nativeValue;
    }


}
