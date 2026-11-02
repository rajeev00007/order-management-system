package com.oms.order;
import com.oms.user.*; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.http.HttpStatus; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal; import java.util.*;
@RestController @RequestMapping("/api/orders") public class OrderController {
 private final OrderRepository orders;private final UserRepository users;public OrderController(OrderRepository o,UserRepository u){orders=o;users=u;}
 public record Request(@NotBlank String customerName,@Email @NotBlank String customerEmail,@NotNull @DecimalMin("0.01") BigDecimal amount,String description,OrderStatus status){}
 private AppUser current(Authentication a){return users.findByEmailIgnoreCase(a.getName()).orElseThrow();}
 private Map<String,Object> dto(CustomerOrder o){return Map.of("id",o.getId(),"customerName",o.getCustomerName(),"customerEmail",o.getCustomerEmail(),"amount",o.getAmount(),"status",o.getStatus().name(),"description",o.getDescription(),"createdAt",o.getCreatedAt(),"createdBy",o.getCreatedBy().getFullName());}
 @GetMapping public List<Map<String,Object>> list(Authentication a){AppUser u=current(a);return (u.getRole()==Role.ADMIN?orders.findAllByOrderByCreatedAtDesc():orders.findByCreatedByIdOrderByCreatedAtDesc(u.getId())).stream().map(this::dto).toList();}
 @PostMapping public Map<String,Object> create(@Valid @RequestBody Request r,Authentication a){return dto(orders.save(new CustomerOrder(r.customerName(),r.customerEmail(),r.amount(),r.description(),current(a))));}
 @PutMapping("/{id}") public Map<String,Object> update(@PathVariable Long id,@Valid @RequestBody Request r,Authentication a){CustomerOrder o=orders.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));AppUser u=current(a);if(u.getRole()!=Role.ADMIN&&!o.getCreatedBy().getId().equals(u.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);o.update(r.customerName(),r.customerEmail(),r.amount(),r.description(),r.status());return dto(orders.save(o));}
 @DeleteMapping("/{id}") @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')") public void delete(@PathVariable Long id){orders.deleteById(id);}
}
