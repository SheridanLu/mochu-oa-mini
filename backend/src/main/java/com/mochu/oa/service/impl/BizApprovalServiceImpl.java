package com.mochu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mochu.oa.entity.*;
import com.mochu.oa.mapper.*;
import com.mochu.oa.service.BizApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizApprovalServiceImpl extends ServiceImpl<BizApprovalInstanceMapper, BizApprovalInstance> implements BizApprovalService {
    
    private final BizApprovalDefMapper bizApprovalDefMapper;
    private final BizApprovalRecordMapper bizApprovalRecordMapper;
    private final BizApprovalTodoMapper bizApprovalTodoMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    @Transactional
    public Long startApproval(String bizType, Long bizId, String title, Long applicantId, String applicantName) {
        BizApprovalDef def = getFlowDef(bizType);
        if (def == null) {
            throw new RuntimeException("审批流程未定义: " + bizType);
        }
        
        List<Map<String, Object>> nodes = parseFlowNodes(def.getFlowJson());
        if (nodes.isEmpty()) {
            throw new RuntimeException("审批流程节点为空");
        }
        
        Map<String, Object> firstNode = nodes.get(0);
        
        BizApprovalInstance instance = new BizApprovalInstance();
        instance.setBizType(bizType);
        instance.setBizId(bizId);
        instance.setApplicantId(applicantId);
        instance.setApplicantName(applicantName);
        instance.setTitle(title);
        instance.setStatus("APPROVING");
        instance.setCurrentNodeOrder(1);
        instance.setCurrentNodeName((String) firstNode.get("name"));
        instance.setStartTime(LocalDateTime.now());
        save(instance);
        
        Long handlerId = resolveHandler(firstNode, applicantId);
        createTodo(instance, 1, (String) firstNode.get("name"), handlerId);
        
        return instance.getId();
    }
    
    @Override
    @Transactional
    public void approve(Long instanceId, Long approverId, String approverName, String action, String opinion) {
        BizApprovalInstance instance = getById(instanceId);
        if (instance == null) throw new RuntimeException("审批实例不存在");
        
        if (!"APPROVING".equals(instance.getStatus())) {
            throw new RuntimeException("当前状态不允许审批");
        }
        
        BizApprovalDef def = getFlowDef(instance.getBizType());
        List<Map<String, Object>> nodes = parseFlowNodes(def.getFlowJson());
        int currentOrder = instance.getCurrentNodeOrder();
        
        Map<String, Object> currentNode = nodes.get(currentOrder - 1);
        saveRecord(instanceId, currentOrder, (String) currentNode.get("name"), approverId, approverName, action, opinion);
        
        updateTodoStatus(instanceId, currentOrder, approverId);
        
        if ("REJECT".equals(action)) {
            instance.setStatus("REJECTED");
            instance.setEndTime(LocalDateTime.now());
            updateById(instance);
            createTodo(instance, currentOrder, (String) currentNode.get("name"), instance.getApplicantId());
            return;
        }
        
        if (currentOrder >= nodes.size()) {
            instance.setStatus("APPROVED");
            instance.setEndTime(LocalDateTime.now());
            updateById(instance);
            log.info("审批完成 bizType={} bizId={}", instance.getBizType(), instance.getBizId());
        } else {
            Map<String, Object> nextNode = nodes.get(currentOrder);
            instance.setCurrentNodeOrder(currentOrder + 1);
            instance.setCurrentNodeName((String) nextNode.get("name"));
            updateById(instance);
            
            Long handlerId = resolveHandler(nextNode, instance.getApplicantId());
            createTodo(instance, currentOrder + 1, (String) nextNode.get("name"), handlerId);
        }
    }
    
    @Override
    @Transactional
    public void delegate(Long instanceId, Long fromUserId, Long toUserId, String opinion) {
        BizApprovalInstance instance = getById(instanceId);
        BizApprovalDef def = getFlowDef(instance.getBizType());
        List<Map<String, Object>> nodes = parseFlowNodes(def.getFlowJson());
        int currentOrder = instance.getCurrentNodeOrder();
        
        saveRecord(instanceId, currentOrder, instance.getCurrentNodeName(), fromUserId, "转办人", "TRANSFER", opinion);
        
        updateTodoStatus(instanceId, currentOrder, fromUserId);
        
        createTodo(instance, currentOrder, instance.getCurrentNodeName(), toUserId);
    }
    
    @Override
    @Transactional
    public void withdraw(Long instanceId, Long userId) {
        BizApprovalInstance instance = getById(instanceId);
        if (!instance.getApplicantId().equals(userId)) {
            throw new RuntimeException("只有申请人可以撤回");
        }
        
        instance.setStatus("WITHDRAWN");
        instance.setEndTime(LocalDateTime.now());
        updateById(instance);
        
        bizApprovalTodoMapper.delete(new LambdaQueryWrapper<BizApprovalTodo>()
                .eq(BizApprovalTodo::getInstanceId, instanceId));
    }
    
    @Override
    @Transactional
    public void cancel(Long instanceId) {
        BizApprovalInstance instance = getById(instanceId);
        instance.setStatus("CANCELLED");
        instance.setEndTime(LocalDateTime.now());
        updateById(instance);
        
        bizApprovalTodoMapper.delete(new LambdaQueryWrapper<BizApprovalTodo>()
                .eq(BizApprovalTodo::getInstanceId, instanceId));
    }
    
    @Override
    public Map<String, Object> getTodoList(Long userId, String category, String bizType, String keyword, int page, int size) {
        String cat = null;
        if (StringUtils.hasText(category)) {
            cat = category.trim().toUpperCase(Locale.ROOT);
            if (!"TODO".equals(cat) && !"DONE".equals(cat) && !"READ".equals(cat)) {
                cat = "TODO";
            }
        }

        Page<BizApprovalTodo> p = new Page<>(page, size);
        LambdaQueryWrapper<BizApprovalTodo> wrapper = new LambdaQueryWrapper<BizApprovalTodo>()
                .eq(BizApprovalTodo::getHandlerId, userId)
                .eq(cat != null, BizApprovalTodo::getCategory, cat)
                .orderByDesc(BizApprovalTodo::getCreatedAt);

        if (StringUtils.hasText(bizType)) {
            String bt = bizType.trim().toUpperCase(Locale.ROOT);
            List<Long> instanceIds = list(new LambdaQueryWrapper<BizApprovalInstance>()
                            .select(BizApprovalInstance::getId)
                            .eq(BizApprovalInstance::getBizType, bt))
                    .stream().map(BizApprovalInstance::getId).collect(Collectors.toList());
            if (instanceIds.isEmpty()) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("list", Collections.emptyList());
                empty.put("total", 0L);
                return empty;
            }
            wrapper.in(BizApprovalTodo::getInstanceId, instanceIds);
        }

        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            List<Long> titleHits = list(new LambdaQueryWrapper<BizApprovalInstance>()
                            .select(BizApprovalInstance::getId)
                            .like(BizApprovalInstance::getTitle, kw))
                    .stream().map(BizApprovalInstance::getId).collect(Collectors.toList());
            wrapper.and(w -> {
                w.like(BizApprovalTodo::getNodeName, kw);
                if (!titleHits.isEmpty()) {
                    w.or().in(BizApprovalTodo::getInstanceId, titleHits);
                }
            });
        }

        IPage<BizApprovalTodo> result = bizApprovalTodoMapper.selectPage(p, wrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        for (BizApprovalTodo todo : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", todo.getId());
            map.put("todoNo", "TD-" + todo.getId());
            map.put("instanceId", todo.getInstanceId());
            map.put("nodeName", todo.getNodeName());
            map.put("currentNode", todo.getNodeName());
            map.put("category", todo.getCategory());
            int pr = todo.getPriority() == null || todo.getPriority() == 0 ? 3 : todo.getPriority();
            map.put("priority", pr);
            map.put("createdAt", todo.getCreatedAt());

            BizApprovalInstance instance = getById(todo.getInstanceId());
            if (instance != null) {
                map.put("title", instance.getTitle());
                map.put("bizType", instance.getBizType());
                map.put("applicantName", instance.getApplicantName());
            }
            map.put("projectName", "");
            list.add(map);
        }
        Map<String, Object> out = new HashMap<>();
        out.put("list", list);
        out.put("total", result.getTotal());
        return out;
    }
    
    @Override
    public List<Map<String, Object>> getHistory(Long instanceId) {
        List<BizApprovalRecord> records = bizApprovalRecordMapper.selectList(
                new LambdaQueryWrapper<BizApprovalRecord>()
                        .eq(BizApprovalRecord::getInstanceId, instanceId)
                        .orderByAsc(BizApprovalRecord::getNodeOrder));
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (BizApprovalRecord r : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("nodeOrder", r.getNodeOrder());
            map.put("nodeName", r.getNodeName());
            map.put("approverName", r.getApproverName());
            map.put("action", r.getAction());
            map.put("opinion", r.getOpinion());
            map.put("operateTime", r.getOperateTime());
            list.add(map);
        }
        return list;
    }
    
    @Override
    public IPage<BizApprovalTodo> getTodoPage(Long userId, String category, String bizType, int page, int size) {
        return bizApprovalTodoMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<BizApprovalTodo>()
                        .eq(BizApprovalTodo::getHandlerId, userId)
                        .eq(category != null, BizApprovalTodo::getCategory, category)
                        .orderByDesc(BizApprovalTodo::getCreatedAt));
    }
    
    @Override
    public long getTodoCount(Long userId) {
        return bizApprovalTodoMapper.selectCount(
                new LambdaQueryWrapper<BizApprovalTodo>()
                        .eq(BizApprovalTodo::getHandlerId, userId)
                        .eq(BizApprovalTodo::getCategory, "TODO")
                        .eq(BizApprovalTodo::getStatus, 1));
    }
    
    @Override
    public long getDoneCount(Long userId) {
        return bizApprovalTodoMapper.selectCount(
                new LambdaQueryWrapper<BizApprovalTodo>()
                        .eq(BizApprovalTodo::getHandlerId, userId)
                        .eq(BizApprovalTodo::getCategory, "DONE"));
    }
    
    @Override
    public long getReadCount(Long userId) {
        return bizApprovalTodoMapper.selectCount(
                new LambdaQueryWrapper<BizApprovalTodo>()
                        .eq(BizApprovalTodo::getHandlerId, userId)
                        .eq(BizApprovalTodo::getCategory, "READ"));
    }
    
    @Override
    public void initFlowDefinitions() {
        initProjectFlow();
        initContractFlow();
        initPurchaseFlow();
        initExpenseFlow();
        initPaymentFlow();
    }
    
    private void initProjectFlow() {
        String json = """
            {"nodes":[{"order":1,"type":"APPROVE","name":"预算员审批","role":"BUDGET","timeout":86400},{"order":2,"type":"APPROVE","name":"总经理审批","role":"GM","timeout":86400}]}
            """;
        saveOrUpdateDef("PROJECT", "项目立项审批", json);
    }
    
    private void initContractFlow() {
        String json = """
            {"nodes":[{"order":1,"type":"APPROVE","name":"法务审批","role":"LEGAL","timeout":86400},{"order":2,"type":"APPROVE","name":"总经理审批","role":"GM","timeout":86400}]}
            """;
        saveOrUpdateDef("CONTRACT", "合同审批", json);
    }
    
    private void initPurchaseFlow() {
        String json = """
            {"nodes":[{"order":1,"type":"APPROVE","name":"预算员审批","role":"BUDGET","timeout":86400},{"order":2,"type":"APPROVE","name":"财务审批","role":"FINANCE","timeout":86400},{"order":3,"type":"APPROVE","name":"总经理审批","role":"GM","timeout":86400}]}
            """;
        saveOrUpdateDef("PURCHASE", "采购审批", json);
    }
    
    private void initExpenseFlow() {
        String json = """
            {"nodes":[{"order":1,"type":"APPROVE","name":"部门主管审批","role":"DEPT_MANAGER","timeout":86400},{"order":2,"type":"APPROVE","name":"财务审批","role":"FINANCE","timeout":86400}]}
            """;
        saveOrUpdateDef("EXPENSE", "报销审批", json);
    }
    
    private void initPaymentFlow() {
        String json = """
            {"nodes":[{"order":1,"type":"APPROVE","name":"财务审批","role":"FINANCE","timeout":86400},{"order":2,"type":"APPROVE","name":"总经理审批","role":"GM","timeout":86400}]}
            """;
        saveOrUpdateDef("PAYMENT", "付款审批", json);
    }
    
    private void saveOrUpdateDef(String bizType, String bizName, String flowJson) {
        BizApprovalDef existing = bizApprovalDefMapper.selectOne(
                new LambdaQueryWrapper<BizApprovalDef>().eq(BizApprovalDef::getBizType, bizType));
        
        if (existing == null) {
            BizApprovalDef def = new BizApprovalDef();
            def.setBizType(bizType);
            def.setBizName(bizName);
            def.setFlowJson(flowJson);
            def.setStatus(1);
            def.setVersion(1);
            bizApprovalDefMapper.insert(def);
        }
    }
    
    @Override
    public BizApprovalDef getFlowDef(String bizType) {
        return bizApprovalDefMapper.selectOne(
                new LambdaQueryWrapper<BizApprovalDef>()
                        .eq(BizApprovalDef::getBizType, bizType)
                        .eq(BizApprovalDef::getStatus, 1));
    }
    
    private List<Map<String, Object>> parseFlowNodes(String flowJson) {
        try {
            JsonNode root = objectMapper.readTree(flowJson);
            JsonNode nodes = root.get("nodes");
            List<Map<String, Object>> result = new ArrayList<>();
            if (nodes != null && nodes.isArray()) {
                for (JsonNode node : nodes) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("order", node.get("order").asInt());
                    map.put("type", node.get("type").asText());
                    map.put("name", node.get("name").asText());
                    map.put("role", node.has("role") ? node.get("role").asText() : null);
                    result.add(map);
                }
            }
            return result;
        } catch (JsonProcessingException e) {
            log.error("解析流程定义失败", e);
            return Collections.emptyList();
        }
    }
    
    private Long resolveHandler(Map<String, Object> node, Long applicantId) {
        return applicantId + 1;
    }
    
    private void createTodo(BizApprovalInstance instance, int nodeOrder, String nodeName, Long handlerId) {
        BizApprovalTodo todo = new BizApprovalTodo();
        todo.setInstanceId(instance.getId());
        todo.setNodeOrder(nodeOrder);
        todo.setNodeName(nodeName);
        todo.setHandlerId(handlerId);
        todo.setHandlerName("审批人");
        todo.setCategory("TODO");
        todo.setPriority(0);
        todo.setStatus(1);
        bizApprovalTodoMapper.insert(todo);
    }
    
    private void updateTodoStatus(Long instanceId, int nodeOrder, Long handlerId) {
        BizApprovalTodo updateTodo = new BizApprovalTodo();
        updateTodo.setStatus(2);
        bizApprovalTodoMapper.update(updateTodo,
                new LambdaQueryWrapper<BizApprovalTodo>()
                        .eq(BizApprovalTodo::getInstanceId, instanceId)
                        .eq(BizApprovalTodo::getNodeOrder, nodeOrder)
                        .eq(BizApprovalTodo::getHandlerId, handlerId));
        
        BizApprovalTodo doneTodo = new BizApprovalTodo();
        doneTodo.setCategory("DONE");
        doneTodo.setStatus(2);
        bizApprovalTodoMapper.update(doneTodo,
                new LambdaQueryWrapper<BizApprovalTodo>()
                        .eq(BizApprovalTodo::getInstanceId, instanceId)
                        .eq(BizApprovalTodo::getHandlerId, handlerId));
    }
    
    private void saveRecord(Long instanceId, int nodeOrder, String nodeName, Long approverId, String approverName, String action, String opinion) {
        BizApprovalRecord record = new BizApprovalRecord();
        record.setInstanceId(instanceId);
        record.setNodeOrder(nodeOrder);
        record.setNodeName(nodeName);
        record.setApproverId(approverId);
        record.setApproverName(approverName);
        record.setAction(action);
        record.setOpinion(opinion);
        record.setOperateTime(LocalDateTime.now());
        bizApprovalRecordMapper.insert(record);
    }
}