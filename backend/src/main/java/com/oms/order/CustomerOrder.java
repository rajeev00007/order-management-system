package com.oms.order;
import com.oms.user.AppUser; import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant;
@Entity @Table(name="customer_orders")
public class CustomerOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String customerName;
 @Column(nullable=false) private String customerEmail;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private OrderStatus status=OrderStatus.PENDING;
 @Column(nullable=false,length=2000) private String description="";
 @ManyToOne(optional=false,fetch=FetchType.EAGER) private AppUser createdBy;
 private Instant createdAt=Instant.now();
 protected CustomerOrder(){}
 public CustomerOrder(String n,String e,BigDecimal a,String d,AppUser u){customerName=n;customerEmail=e;amount=a;description=d==null?"":d;createdBy=u;}
 public Long getId(){return id;} public String getCustomerName(){return customerName;} public String getCustomerEmail(){return customerEmail;}
 public BigDecimal getAmount(){return amount;} public OrderStatus getStatus(){return status;} public String getDescription(){return description;}
 public AppUser getCreatedBy(){return createdBy;} public Instant getCreatedAt(){return createdAt;}
 public void update(String n,String e,BigDecimal a,String d,OrderStatus s){customerName=n;customerEmail=e;amount=a;description=d==null?"":d;if(s!=null)status=s;}
}
