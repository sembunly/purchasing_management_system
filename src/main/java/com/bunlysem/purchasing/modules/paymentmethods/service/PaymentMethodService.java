package com.bunlysem.purchasing.modules.paymentmethods.service;

import com.bunlysem.purchasing.modules.paymentmethods.model.PaymentMethod;
import com.bunlysem.purchasing.modules.paymentmethods.repository.PaymentMethodRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service @Transactional
public class PaymentMethodService {
    private final PaymentMethodRepository methods;
    private final JdbcTemplate jdbc;
    public PaymentMethodService(PaymentMethodRepository methods, JdbcTemplate jdbc) { this.methods=methods; this.jdbc=jdbc; }
    @Transactional(readOnly=true) public PaymentMethod get(Long id) {
        return methods.findByIdAndIsActive(id,1).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Payment method not found."));
    }
    public PaymentMethod save(Long id,String code,String name) {
        String c=code==null?"":code.trim().toUpperCase(java.util.Locale.ROOT), n=name==null?"":name.trim();
        if(!c.matches("[A-Z0-9_-]{2,40}")) throw new IllegalArgumentException("Code must be 2–40 letters, numbers, dashes or underscores.");
        if(n.isBlank()||n.length()>100) throw new IllegalArgumentException("Name is required and must be at most 100 characters.");
        if(id==null?methods.existsByCodeIgnoreCase(c):methods.existsByCodeIgnoreCaseAndIdNot(c,id)) throw new IllegalArgumentException("This payment method code already exists.");
        PaymentMethod method=id==null?new PaymentMethod():get(id); method.setCode(c); method.setName(n); return methods.save(method);
    }
    public void deactivate(Long id) {
        PaymentMethod m=get(id);
        Integer used=jdbc.queryForObject("select count(*) from payments where payment_method_id=? and is_active=1",Integer.class,id);
        if(used!=null&&used>0) throw new IllegalArgumentException("This payment method is used by active payments and cannot be deactivated.");
        m.setIsActive(0);
    }
}
