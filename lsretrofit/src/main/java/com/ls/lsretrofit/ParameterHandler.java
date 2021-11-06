package com.ls.lsretrofit;

public abstract class ParameterHandler {

    public abstract void apply(ServiceMethod method,String value);

    public static class FieldParameterHandler extends ParameterHandler {

        private String mKey;

        public FieldParameterHandler(String key){
            mKey = key;
        }

        @Override
        public void apply(ServiceMethod method,String value) {
            method.addFieldParameter(mKey,value);
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
