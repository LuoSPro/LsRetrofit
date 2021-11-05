package com.ls.lsretrofit;


import com.ls.lsretrofit.annotation.Field;
import com.ls.lsretrofit.annotation.GET;
import com.ls.lsretrofit.annotation.POST;
import com.ls.lsretrofit.annotation.Query;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class ServiceMethod {

    private final ParameterHandler[] parameterHandlers;
    private final Annotation[][] parameterAnnotations;
    private final Annotation[] methodAnnotations;
    private final boolean hasBody;
    private final String requestMethod;
    private final String requestUrl;
    private FormBody.Builder formBodyBuilder;
    private HttpUrl httpUrl;
    private HttpUrl.Builder httpUrlBuilder;
    private final Call.Factory callFactory;

    /**
     * 把builder的属性同步到serviceMethod中来
     * 并且判断是否有body
     * @param builder
     */
    public ServiceMethod(Builder builder){
        httpUrl = builder.mRetrofit.baseUrl;
        callFactory = builder.mRetrofit.callFactory;

        parameterHandlers = builder.parameterHandlers;
        parameterAnnotations = builder.parameterAnnotations;
        methodAnnotations = builder.methodAnnotations;
        hasBody = builder.hasBody;
        requestMethod = builder.requestMethod;
        requestUrl = builder.requestUrl;

        if (hasBody){
            formBodyBuilder = new FormBody.Builder();
        }
    }

    /**
     * 一、处理请求地址与参数
     * 这个invoke方法是给动态代理的invoke方法调用的，因为代理方法的参数值就保存在他的args数组里面，那边传过来后，在这里就能
     * 收到了
     * 在Builder中已经解析出了Handler对象，并保存了key，这里的args里面又有value，所以就能处理了
     * @param args
     */
    public Object invoke(Object[] args){
        for (int i = 0; i < parameterAnnotations.length; i++) {
            parameterHandlers[i].apply(this,args[i].toString());
        }

        //解析完，开始请求
        if (httpUrlBuilder == null){
            httpUrlBuilder = httpUrl.newBuilder(requestUrl);
        }
        HttpUrl url = httpUrlBuilder.build();

        FormBody formBody = null;
        if (formBodyBuilder != null){
            formBody = formBodyBuilder.build();
        }

        Request request = new Request.Builder().url(url).method(requestMethod, formBody).build();
        return callFactory.newCall(request);
    }

    public void addFiledParameter(String key, String value) {
        formBodyBuilder.add(key,value);
    }

    public void addQueryParameter(String key, String value) {
        if (httpUrlBuilder == null){
            httpUrlBuilder = httpUrl.newBuilder();
        }
        httpUrlBuilder.addQueryParameter(key,value);
    }

    public static class Builder{

        private LsRetrofit mRetrofit;
        private Method mMethod;
        private String requestMethod;
        private String requestUrl;
        private boolean hasBody;
        private Annotation[] methodAnnotations;
        private Annotation[][] parameterAnnotations;
        private ParameterHandler[] parameterHandlers;

        /**
         * 解析方法
         * @param method
         */
        public Builder(LsRetrofit retrofit, Method method){
            mRetrofit = retrofit;
            mMethod = method;
        }

        /**
         * 一、解析方法上的注解：POST、GET
         * 1. 判断方法
         * 2. 记录当前请求的方式
         * 3. 记录当前请求的url
         * 4. 是否有请求体
         *
         * 二、解析方法参数的注解
         * 1. 获取一个参数上面的所有注解
         * 2. 处理参数上面的每一个注解
         *   2.1 判断这个注解：Filed、Query。、、todo：如果判断为filed，并且如果方法是get，可以提醒开发者使用query注解
         *   2.2 得到注解上的value：请求参数的key  --> 这里用到了Handler去处理，因为filed和query会涉及到文件
         * @return
         */
        public ServiceMethod build(){
            methodAnnotations = mMethod.getAnnotations();
            for (Annotation annotation : methodAnnotations) {
                if (annotation instanceof GET){
                    requestMethod = "GET";
                    requestUrl = ((GET) annotation).value();
                    hasBody = false;
                }else if(annotation instanceof POST){
                    requestMethod = "POST";
                    requestUrl = ((POST) annotation).value();
                    hasBody = true;
                }
            }

            parameterAnnotations = mMethod.getParameterAnnotations();
            parameterHandlers = new ParameterHandler[parameterAnnotations.length];
            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    String value;
                    if (annotation instanceof Field){
                        value = ((Field) annotation).value();
                        parameterHandlers[i] = new ParameterHandler.FiledParameterHandler(value);
                    }else if(annotation instanceof Query){
                        value = ((Query) annotation).value();
                        parameterHandlers[i] = new ParameterHandler.FiledParameterHandler(value);
                    }
                }
            }
            return new ServiceMethod(this);
        }

    }

}
