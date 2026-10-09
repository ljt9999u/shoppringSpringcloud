package org.example.Controller;

import org.example.Mapper.StatsMapper;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestParam;

/**
 * 平台数据看板统计（管理员端）
 * GET /api/user/admin/stats
 * 聚合 user / product / merchant / orders 四张表（同库）
 */
@RestController
@RequestMapping("/api/user/admin")
public class AdminStatsController {

    private static final int TREND_MONTHS = 6;

    @Autowired
    private StatsMapper statsMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("totalUsers", statsMapper.countUsers());
            data.put("totalProducts", statsMapper.countProductsOnSale());
            data.put("totalMerchants", statsMapper.countMerchants());
            data.put("totalOrders", statsMapper.countOrders());
            data.put("totalRevenue", statsMapper.sumRevenue());

            // 近 6 个月月份标签
            LocalDate now = LocalDate.now();
            LocalDate start = now.minusMonths(TREND_MONTHS - 1L).withDayOfMonth(1);
            String startStr = start.toString();
            List<String> months = new ArrayList<>();
            for (int i = 0; i < TREND_MONTHS; i++) {
                months.add(start.plusMonths(i).toString().substring(0, 7));
            }

            data.put("months", months);
            data.put("userTrend", fillMonths(months, statsMapper.userTrend(startStr), "cnt"));
            data.put("merchantTrend", fillMonths(months, statsMapper.merchantTrend(startStr), "cnt"));
            data.put("productTrend", fillMonths(months, statsMapper.productTrend(startStr), "cnt"));
            data.put("revenueTrend", fillMonths(months, statsMapper.revenueTrend(startStr), "amount"));
            data.put("categoryPie", statsMapper.categoryPie());
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail("统计数据加载失败：" + e.getMessage());
        }
    }

    /** 把 SQL 按月结果补齐为连续 N 个月（缺月补 0），返回 [{month, count}] */
    private List<Map<String, Object>> fillMonths(List<String> months, List<Map<String, Object>> rows, String valueKey) {
        Map<String, Object> indexed = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String month = String.valueOf(row.get("month"));
            indexed.put(month, row.get(valueKey));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (String month : months) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", month);
            Object v = indexed.get(month);
            item.put("count", v == null ? 0 : v);
            result.add(item);
        }
        return result;
    }

    /**
     * 图表明细下钻（点击图表数据点弹窗展示）
     * GET /api/user/admin/stats/detail?type=user|merchant|product|revenue&month=2026-10
     * GET /api/user/admin/stats/detail?type=category&name=茶具雅器
     */
    @GetMapping("/stats/detail")
    public Result<Map<String, Object>> statsDetail(
            @RequestParam String type,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String name) {
        try {
            List<Map<String, Object>> rows;
            String title;
            if ("category".equals(type)) {
                if (name == null || name.trim().isEmpty()) {
                    return Result.fail("缺少分类名称");
                }
                rows = statsMapper.categoryDetail(name.trim());
                title = "「" + name.trim() + "」在售商品明细";
            } else {
                if (month == null || !month.matches("\\d{4}-\\d{2}")) {
                    return Result.fail("月份格式错误");
                }
                LocalDate start = LocalDate.parse(month + "-01");
                LocalDate end = start.plusMonths(1);
                String s = start.toString();
                String e = end.toString();
                switch (type) {
                    case "user":
                        rows = statsMapper.userDetail(s, e);
                        title = month + " 注册用户明细";
                        break;
                    case "merchant":
                        rows = statsMapper.merchantDetail(s, e);
                        title = month + " 商家申请明细";
                        break;
                    case "product":
                        rows = statsMapper.productDetail(s, e);
                        title = month + " 商品发布明细";
                        break;
                    case "revenue":
                        rows = statsMapper.revenueDetail(s, e);
                        title = month + " 已支付订单明细";
                        break;
                    default:
                        return Result.fail("未知的明细类型：" + type);
                }
            }
            Map<String, Object> data = new HashMap<>();
            data.put("title", title);
            data.put("rows", rows);
            data.put("truncated", rows.size() >= 50);
            return Result.success(data);
        } catch (Exception ex) {
            return Result.fail("明细查询失败：" + ex.getMessage());
        }
    }
}
