package org.example.common;

import lombok.Data;

import java.util.List;

/**
 * 通用分页结果
 */
@Data
public class PageResult<T> {
    private Long total;       // 总记录数
    private Integer pageNum;   // 当前页码
    private Integer pageSize;   // 每页条数
    private Integer pages;      // 总页数
    private List<T> list;      // 数据列表

    public PageResult() {}

    public PageResult(Long total, Integer pageNum, Integer pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
        this.list = list;
    }
}
