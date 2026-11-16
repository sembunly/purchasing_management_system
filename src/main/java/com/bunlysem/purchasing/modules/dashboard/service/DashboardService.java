package com.bunlysem.purchasing.modules.dashboard.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final EntityManager entityManager;

    public DashboardService(EntityManager entityManager) { this.entityManager = entityManager; }

    public record Totals(long products, long suppliers, long requests, long orders,
                         long pending, BigDecimal spending) { }
    public record StatusCount(String name, long count, int percent) { }
    public record DepartmentSpend(String name, BigDecimal amount, int percent) { }
    public record Activity(String type, String reference, String detail, String status, LocalDateTime date) { }

    public Totals totals() {
        Object[] values = (Object[]) entityManager.createNativeQuery("""
            select (select count(*) from products where is_active=1),
                   (select count(*) from suppliers where is_active=1),
                   (select count(*) from purchase_requests where is_active=1),
                   (select count(*) from purchase_orders where is_active=1),
                   (select count(*) from purchase_requests where is_active=1 and status='PENDING'),
                   (select coalesce(sum(p.amount),0) from payments p
                    join purchase_orders po on po.id=p.purchase_order_id
                    where p.is_active=1 and po.is_active=1)
            from dual
            """).getSingleResult();
        return new Totals(number(values[0]).longValue(), number(values[1]).longValue(),
            number(values[2]).longValue(), number(values[3]).longValue(),
            number(values[4]).longValue(), decimal(values[5]));
    }

    public List<StatusCount> requestStatuses() {
        var counts = rows("""
            select status,count(*) from purchase_requests where is_active=1
            group by status order by status
            """).stream().map(row -> new StatusCount((String) row[0], number(row[1]).longValue(), 0)).toList();
        long total = counts.stream().mapToLong(StatusCount::count).sum();
        return counts.stream().map(item -> new StatusCount(item.name(), item.count(),
            total == 0 ? 0 : (int) Math.round(item.count() * 100.0 / total))).toList();
    }

    public List<DepartmentSpend> departmentSpend() {
        var amounts = rows("""
            select d.name,coalesce(sum(pod.quantity*pod.unit_price),0) amount
            from departments d
            join purchase_requests pr on pr.department_id=d.id and pr.is_active=1
            join purchase_orders po on po.purchase_request_id=pr.id and po.is_active=1
            join purchase_order_details pod on pod.purchase_order_id=po.id and pod.is_active=1
            where d.is_active=1 group by d.id,d.name order by amount desc
            fetch first 5 rows only
            """).stream().map(row -> new DepartmentSpend((String) row[0], decimal(row[1]), 0)).toList();
        BigDecimal max = amounts.stream().map(DepartmentSpend::amount).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        return amounts.stream().map(item -> new DepartmentSpend(item.name(), item.amount(),
            max.signum() == 0 ? 0 : item.amount().multiply(BigDecimal.valueOf(100))
                .divide(max, 0, java.math.RoundingMode.HALF_UP).intValue())).toList();
    }

    public List<Activity> recentActivity() {
        return rows("""
            select kind,reference,detail,status,event_date from (
              select 'Request' kind,pr.request_no reference,d.name detail,pr.status,
                     pr.request_date event_date
              from purchase_requests pr join departments d on d.id=pr.department_id
              where pr.is_active=1
              union all
              select 'Order',po.order_no,s.name,po.status,po.order_date
              from purchase_orders po join suppliers s on s.id=po.supplier_id
              where po.is_active=1
              union all
              select 'Receipt',gr.receipt_no,po.order_no,'RECEIVED',gr.received_at
              from goods_receipts gr join purchase_orders po on po.id=gr.purchase_order_id
              where gr.is_active=1 and po.is_active=1
            ) order by event_date desc fetch first 8 rows only
            """).stream().map(row -> new Activity((String) row[0], (String) row[1],
                (String) row[2], (String) row[3],
                dateTime(row[4]))).toList();
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> rows(String sql) {
        return entityManager.createNativeQuery(sql).getResultList();
    }

    private Number number(Object value) { return (Number) value; }
    private BigDecimal decimal(Object value) { return value instanceof BigDecimal bd ? bd : new BigDecimal(value.toString()); }
    private LocalDateTime dateTime(Object value) {
        return value instanceof LocalDateTime date ? date : ((java.sql.Timestamp) value).toLocalDateTime();
    }
}
