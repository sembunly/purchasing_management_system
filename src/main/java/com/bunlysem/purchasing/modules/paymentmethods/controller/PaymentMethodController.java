package com.bunlysem.purchasing.modules.paymentmethods.controller;

import com.bunlysem.purchasing.modules.paymentmethods.model.PaymentMethod;
import com.bunlysem.purchasing.modules.paymentmethods.repository.PaymentMethodRepository;
import com.bunlysem.purchasing.modules.paymentmethods.service.PaymentMethodService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/payment-methods")
public class PaymentMethodController {
    private final PaymentMethodRepository methods; private final PaymentMethodService service;
    public PaymentMethodController(PaymentMethodRepository methods,PaymentMethodService service){this.methods=methods;this.service=service;}
    @GetMapping String list(Model model){model.addAttribute("activeCode","PAYMENT_METHODS");model.addAttribute("methods",methods.findByIsActiveOrderByNameAsc(1));return "paymentmethods/index";}
    @GetMapping("/new") String createForm(Model model){return form(model,null);}
    @GetMapping("/{id}/edit") String editForm(@PathVariable Long id,Model model){return form(model,service.get(id));}
    @PostMapping String create(@RequestParam String code,@RequestParam String name,RedirectAttributes redirect,Model model){return save(null,code,name,redirect,model);}
    @PostMapping("/{id}") String update(@PathVariable Long id,@RequestParam String code,@RequestParam String name,RedirectAttributes redirect,Model model){return save(id,code,name,redirect,model);}
    @PostMapping("/{id}/deactivate") String deactivate(@PathVariable Long id,RedirectAttributes redirect){try{service.deactivate(id);redirect.addFlashAttribute("success","Payment method deactivated.");}catch(IllegalArgumentException ex){redirect.addFlashAttribute("error",ex.getMessage());}return "redirect:/payment-methods";}
    private String save(Long id,String code,String name,RedirectAttributes redirect,Model model){try{service.save(id,code,name);redirect.addFlashAttribute("success","Payment method saved.");return "redirect:/payment-methods";}catch(IllegalArgumentException ex){model.addAttribute("error",ex.getMessage());PaymentMethod m=id==null?new PaymentMethod():service.get(id);m.setCode(code);m.setName(name);return form(model,m);}}
    private String form(Model model,PaymentMethod method){model.addAttribute("activeCode","PAYMENT_METHODS");model.addAttribute("method",method);return "paymentmethods/form";}
}
