package com.oms.user;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="users")
public class AppUser {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String email;
 @Column(nullable=false) private String fullName;
 @Column(nullable=false) private String passwordHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role;
 private Instant createdAt=Instant.now();
 protected AppUser(){}
 public AppUser(String e,String n,String p,Role r){email=e;fullName=n;passwordHash=p;role=r;}
 public Long getId(){return id;} public String getEmail(){return email;} public String getFullName(){return fullName;}
 public String getPasswordHash(){return passwordHash;} public Role getRole(){return role;}
}
