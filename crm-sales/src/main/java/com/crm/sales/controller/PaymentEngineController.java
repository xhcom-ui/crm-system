package com.crm.sales.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.log.OperationLog;
import com.crm.common.result.Result;
import com.crm.sales.entity.PaymentChannel;
import com.crm.sales.entity.PaymentOrder;
import com.crm.sales.entity.PaymentRefund;
import com.crm.sales.mapper.PaymentChannelMapper;
import com.crm.sales.mapper.PaymentOrderMapper;
import com.crm.sales.mapper.PaymentRefundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sales/payment")
@RequiredArgsConstructor
public class PaymentEngineController {

    private final PaymentOrderMapper orderMapper;
    private final PaymentRefundMapper refundMapper;
    private final PaymentChannelMapper channelMapper;

    @GetMapping("/overview")
    @SaCheckPermission("payment:list")
    public Result<Map<String, Object>> overview() {
        List<PaymentOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<>());
        List<PaymentRefund> refunds = refundMapper.selectList(new LambdaQueryWrapper<>());
        BigDecimal paidAmount = orders.stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == 2)
                .map(PaymentOrder::getAmount).filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refundAmount = refunds.stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == 2)
                .map(PaymentRefund::getAmount).filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metrics", List.of(
                metric("支付订单", orders.size(), "统一下单总数"),
                metric("成功金额", "¥" + paidAmount, "已支付订单"),
                metric("退款金额", "¥" + refundAmount, "已成功退款"),
                metric("待对账", orders.stream().filter(item -> !"MATCHED".equals(item.getReconcileStatus())).count(), "渠道账单差异")
        ));
        result.put("flows", List.of("统一下单", "渠道支付", "支付回调", "关单", "退款", "转账", "对账"));
        result.put("missingCapabilities", List.of("支付签名验签", "渠道账单文件解析", "分账规则", "风控限额", "幂等请求表"));
        result.put("capabilities", List.of(
                capability("支付签名验签", "统一封装各渠道签名、验签、证书轮换和回调报文校验。", "/sales/payment/signature"),
                capability("渠道账单文件解析", "解析微信、支付宝、银行卡等渠道账单文件，生成标准对账明细。", "/sales/payment/bill-parser"),
                capability("分账规则", "按商户、门店、销售、服务商等维度配置分账比例和结算周期。", "/sales/payment/profit-sharing"),
                capability("风控限额", "按用户、商户、渠道、单笔、日累计配置支付和退款限额。", "/sales/payment/risk-limits"),
                capability("幂等请求表", "记录下单、退款、转账、回调请求幂等键，防止重复处理。", "/sales/payment/idempotency")
        ));
        return Result.success(result);
    }

    @GetMapping("/orders")
    @SaCheckPermission("payment:list")
    public Result<IPage<PaymentOrder>> orders(@RequestParam(defaultValue = "1") Integer current,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(PaymentOrder::getPaymentNo, keyword)
                    .or().like(PaymentOrder::getMerchantOrderNo, keyword)
                    .or().like(PaymentOrder::getCustomerName, keyword));
        }
        if (status != null) wrapper.eq(PaymentOrder::getStatus, status);
        wrapper.orderByDesc(PaymentOrder::getCreateTime);
        return Result.success(orderMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @GetMapping("/refunds")
    @SaCheckPermission("payment:list")
    public Result<List<PaymentRefund>> refunds(@RequestParam(required = false) String paymentNo) {
        LambdaQueryWrapper<PaymentRefund> wrapper = new LambdaQueryWrapper<>();
        if (paymentNo != null && !paymentNo.isBlank()) wrapper.eq(PaymentRefund::getPaymentNo, paymentNo);
        wrapper.orderByDesc(PaymentRefund::getCreateTime);
        return Result.success(refundMapper.selectList(wrapper));
    }

    @GetMapping("/channels")
    @SaCheckPermission("payment:list")
    public Result<List<PaymentChannel>> channels() {
        return Result.success(channelMapper.selectList(new LambdaQueryWrapper<PaymentChannel>()
                .orderByAsc(PaymentChannel::getChannelCode)));
    }

    @PostMapping("/unified-order")
    @SaCheckPermission("payment:add")
    @OperationLog(title = "支付统一下单", type = "INSERT")
    public Result<Boolean> unifiedOrder(@RequestBody PaymentOrder order) {
        if (order.getPaymentNo() == null || order.getPaymentNo().isBlank()) {
            order.setPaymentNo("P" + System.currentTimeMillis());
        }
        order.setStatus(order.getStatus() == null ? 1 : order.getStatus());
        order.setNotifyStatus(order.getNotifyStatus() == null ? "WAITING" : order.getNotifyStatus());
        order.setReconcileStatus(order.getReconcileStatus() == null ? "PENDING" : order.getReconcileStatus());
        order.setRefundableAmount(order.getRefundableAmount() == null ? order.getAmount() : order.getRefundableAmount());
        return Result.success(orderMapper.insert(order) > 0);
    }

    @PostMapping("/refund")
    @SaCheckPermission("payment:refund")
    @OperationLog(title = "发起支付退款", type = "INSERT")
    public Result<Boolean> refund(@RequestBody PaymentRefund refund) {
        if (refund.getRefundNo() == null || refund.getRefundNo().isBlank()) {
            refund.setRefundNo("R" + System.currentTimeMillis());
        }
        refund.setStatus(refund.getStatus() == null ? 1 : refund.getStatus());
        if (refund.getStatus() == 2) refund.setSuccessTime(LocalDateTime.now());
        return Result.success(refundMapper.insert(refund) > 0);
    }

    private Map<String, Object> metric(String label, Object value, String hint) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        item.put("hint", hint);
        return item;
    }

    private Map<String, Object> capability(String name, String desc, String api) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("desc", desc);
        item.put("api", api);
        item.put("status", "待建设");
        return item;
    }
}
