package org.example.common;

import lombok.Data;

@Data
public class R {
    private Integer code;
    private String msg;
    private Object data;


    public static R ok(String msg, Object data) {
      R r = new R();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    public static R ok() {
        R r = new R();
        r.setCode(200);
        return r;
    }

    public static R err(){
        R r = new R();
        r.setCode(500);
        r.setMsg("系统异常");
        return r;
    }

    public static R err(String msg, Object data) {
        R r = new R();
        r.setCode(500);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    public static R err(Integer code, String msg){
        R r = new R();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
