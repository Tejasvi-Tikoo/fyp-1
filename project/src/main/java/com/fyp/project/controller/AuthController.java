package com.fyp.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fyp.project.config.JwtProvider;
import com.fyp.project.exception.UserException;
import com.fyp.project.model.Users;
import com.fyp.project.repository.UserRepository;
import com.fyp.project.response.AuthResponse;
import com.fyp.project.service.CustomUserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;


    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody Users user) throws UserException{
        String email= user.getEmail();
        String password= user.getPassword();
        String fullName= user.getFullName();
        String birthDate= user.getBirthDate();

        Users isEmailExist = userRepository.findByEmail(email);

        if(isEmailExist != null){
            throw new UserException("Email already exists, is use with another account");
        }

        Users createdUser = new Users();
        createdUser.setEmail(email);
        createdUser.setPassword(passwordEncoder.encode(password));
        createdUser.setFullName(fullName);
        createdUser.setBirthDate(birthDate);

        Users savedUser= userRepository.save(createdUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);

        AuthResponse res = new AuthResponse();
        res.setJwt(token);
        res.setStatus(true);
        res.setMessage("User created successfully");

        return new ResponseEntity<AuthResponse>(res, HttpStatus.CREATED);
    }
    
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody Users user){
        String username= user.getEmail();
        String password= user.getPassword();

        Authentication authentication = authenticate(username, password);
        
        String token = jwtProvider.generateToken(authentication);

        AuthResponse res = new AuthResponse();
        res.setJwt(token);
        res.setStatus(true);
        res.setMessage("User accepted successfully");

        return new ResponseEntity<AuthResponse>(res, HttpStatus.ACCEPTED);
    }


    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        if(userDetails == null){
            throw new BadCredentialsException("Invalid Username..");
        }
        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new BadCredentialsException("Invalid Password..");
        }   
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
