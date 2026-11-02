package com.oms.auth;
import com.oms.security.JwtService; import com.oms.user.*; import jakarta.validation.Valid; import jakarta.validation.constraints.*;
import org.springframework.http.*; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/auth") public class AuthController {
 private final UserRepository users;private final PasswordEncoder encoder;private final JwtService jwt;
 public AuthController(UserRepository u,PasswordEncoder e,JwtService j){users=u;encoder=e;jwt=j;}
 public record Register(@NotBlank String fullName,@Email @NotBlank String email,@Size(min=8) String password){}
 public record Login(@Email @NotBlank String email,@NotBlank String password){}
 private Map<String,Object> result(AppUser u){return Map.of("token",jwt.generate(u.getEmail()),"user",Map.of("id",u.getId(),"fullName",u.getFullName(),"email",u.getEmail(),"role",u.getRole().name()));}
 @PostMapping("/register") public ResponseEntity<?> register(@Valid @RequestBody Register r){
  if(users.findByEmailIgnoreCase(r.email()).isPresent())return ResponseEntity.badRequest().body(Map.of("message","Email already registered"));
  Role role=users.count()==0?Role.ADMIN:Role.EMPLOYEE;return ResponseEntity.ok(result(users.save(new AppUser(r.email().toLowerCase(),r.fullName(),encoder.encode(r.password()),role))));
 }
 @PostMapping("/login") public ResponseEntity<?> login(@Valid @RequestBody Login r){AppUser u=users.findByEmailIgnoreCase(r.email()).orElse(null);if(u==null||!encoder.matches(r.password(),u.getPasswordHash()))return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Invalid email or password"));return ResponseEntity.ok(result(u));}
 @GetMapping("/me") public Map<String,Object> me(org.springframework.security.core.Authentication a){AppUser u=users.findByEmailIgnoreCase(a.getName()).orElseThrow();return Map.of("id",u.getId(),"fullName",u.getFullName(),"email",u.getEmail(),"role",u.getRole().name());}
}
