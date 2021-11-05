package com.ls.lsretrofit;

public abstract class ParameterHandler {

    public abstract void apply(ServiceMethod method,String value);

    public static class FiledParameterHandler extends ParameterHandler {

        private String mKey;

        public FiledParameterHandler(String key){
            mKey = key;
        }

        @Override
        public void apply(ServiceMethod method,String value) {
            method.addFiledParameter(mKey,value);
        }
    }

    public static class QueryParameterHandler extends ParameterHandler {

        private String mKey;

        public QueryParameterHandler(String key){
            mKey = key;
        }

        @Override
        public void apply(ServiceMethod method,String value) {
            method.addQueryParameter(mKey,value);
        }
    }

}
