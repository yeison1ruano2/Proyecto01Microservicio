package com.auth.jwt.service;

import com.auth.jwt.dto.AuthUserDto;
import com.auth.jwt.dto.NewUserDto;
import com.auth.jwt.dto.RequestDto;
import com.auth.jwt.dto.TokenDto;
import com.auth.jwt.entity.AuthUser;
import com.auth.jwt.repository.AuthUserRepository;
import com.auth.jwt.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

  @Autowired
  private AuthUserRepository authUserRepository;

  @Autowired
  private JwtProvider jwtProvider;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public AuthUser save(NewUserDto dto){
    Optional<AuthUser> user = authUserRepository.findByUserName(dto.getUserName());
    if(user.isPresent()){
      return null;
    }
    String password = passwordEncoder.encode(dto.getPassword());
    AuthUser authUser = AuthUser.builder()
            .userName(dto.getUserName())
            .password(password)
            .role(dto.getRole())
            .build();
    return authUserRepository.save(authUser);
  }

  public TokenDto login(AuthUserDto dto){
    Optional<AuthUser> user = authUserRepository.findByUserName(dto.getUserName());
    if(!user.isPresent()){
      return null;
    }
    if(passwordEncoder.matches(dto.getPassword(),user.get().getPassword())){
      return new TokenDto(jwtProvider.createToken(user.get()));
    }
    return null;
  }

  public TokenDto validate(String token, RequestDto requestDto){
    if(!jwtProvider.validate(token,requestDto)){
      return null;
    }
    String userName = jwtProvider.getUserNameFromToken(token);
    if(!authUserRepository.findByUserName(userName).isPresent()){
      return null;
    }
    return new TokenDto(token);
  }
}
