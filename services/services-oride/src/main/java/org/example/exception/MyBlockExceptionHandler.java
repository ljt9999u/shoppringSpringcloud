package org.example.exception;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.common.R;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

//自定义异常处理类web接口处理异常
@Component
public class MyBlockExceptionHandler implements BlockExceptionHandler {

    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpServletRequest Request,
                       HttpServletResponse Response,
                       String resourceName,
                       BlockException e) throws Exception {
        Response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = Response.getWriter();
        R r = R.err(500,"被sentinel限制了原因："+e.getClass());
       String json = objectMapper.writeValueAsString(r);
        writer.write(json);
        writer.flush();//刷
        writer.close();//关闭
    }


}
