package com.ls.lsretrofit;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;


/**
 * 不考虑吧converter和adapter
 */
public class LsRetrofit {

    private final Map<Method, ServiceMethod> serviceMethodCache = new ConcurrentHashMap<>();


    Call.Factory callFactory;
    HttpUrl baseUrl;

    public LsRetrofit(Call.Factory callFactory, HttpUrl baseUrl) {
        this.callFactory = callFactory;
        this.baseUrl = baseUrl;
    }

    public <T> T create(Class<T> service){
        return (T)Proxy.newProxyInstance(service.getClassLoader(),
                new Class[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //交给ServiceMethod的Builder处理
                        ServiceMethod serviceMethod = loadServiceMethod(method);
                        return serviceMethod.invoke(args);
                    }
                });
    }

    private ServiceMethod loadServiceMethod(Method method) {
        //先判断是否有缓存
        ServiceMethod serviceMethod = serviceMethodCache.get(method);

        //没有则采用DLC创建
        if (serviceMethod == null){
            synchronized (serviceMethodCache){
                serviceMethod = serviceMethodCache.get(method);
                if (serviceMethod == null){
                    serviceMethod = new ServiceMethod.Builder(this,method).build();
                    serviceMethodCache.put(method,serviceMethod);
                }
            }
        }
        return serviceMethod;
    }

    public static class Builder{

        private HttpUrl baseUrl;
        private Call.Factory callFactory;

        public Builder baseUrl(String baseUrl){
            this.baseUrl = HttpUrl.get(baseUrl);
            return this;
        }

        public Builder callFactor(Call.Factory callFactory){
            this.callFactory = callFactory;
            return this;
        }

        /**
         * 检查参数，并构建对象
         * @return
         */
        public LsRetrofit build(){
            if (baseUrl == null){
                throw new IllegalStateException("Base URL required.");
            }
            if (callFactory == null){
                callFactory = new OkHttpClient();
            }
            return new LsRetrofit(callFactory,baseUrl);
        }

    }

}
