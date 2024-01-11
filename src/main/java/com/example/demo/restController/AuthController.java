package com.example.demo.restController;

import com.example.demo.Jwt.JwtUtils;
import com.example.demo.MessageResponse;
import com.example.demo.UserInfoResponse;
import com.example.demo.model.ERole;
import com.example.demo.model.Repositiries.RoleRepo;
import com.example.demo.model.Repositiries.UserRepo;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.dto.LogInDto;
import com.example.demo.model.dto.SignUpDto;
import com.example.demo.service.UserDetailsImpl;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;




@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    RoleRepo roleRepo;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    PasswordEncoder bCryptPasswordEncoder;
    @Autowired
    JwtUtils jwtUtils;

     @PostMapping("/signIn")
    public ResponseEntity<?> authenticateUser(@RequestBody LogInDto sign){
         Authentication authentication = authenticationManager
                 .authenticate(new UsernamePasswordAuthenticationToken(sign.getUsername(),sign.getPassword()));
         SecurityContextHolder.getContext().setAuthentication(authentication);

         UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
         ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

         List<String> roles = userDetails.getAuthorities().stream()
                 .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
         return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,jwtCookie.toString())
                 .body(new UserInfoResponse(userDetails.getId(),
                                            userDetails.getUsername(),
                                             userDetails.getEmail(),roles));


    }

    @PostMapping("/signUp")
    public ResponseEntity<?> registration(@RequestBody SignUpDto sign){
        User user = new User();
        user.setUsername(sign.getUsername());
        if (userRepo.existsUserByEmail(sign.getEmail())){
            return new ResponseEntity<>("Error:Account with this email already exists",HttpStatus.BAD_REQUEST);
        }else {
            user.setEmail(sign.getEmail());
        }
        if (sign.getPassword().length()<8){
            return new ResponseEntity<>("Error: This password short",HttpStatus.BAD_REQUEST);
        }else {
            user.setPassword(bCryptPasswordEncoder.encode(sign.getPassword()));
        }
        Set<String> strRoles = sign.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepo.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepo.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepo.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepo.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepo.save(user);
        return new ResponseEntity<>("register sucesful", HttpStatus.OK);


    }
    @PostMapping("/signOut")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }

}
