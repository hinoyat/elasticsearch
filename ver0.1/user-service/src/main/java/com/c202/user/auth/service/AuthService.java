package com.c202.user.auth.service;

import com.c202.user.auth.model.request.LoginRequestDto;
import com.c202.user.auth.model.request.SignupRequestDto;
import com.c202.user.user.model.response.UserResponseDto;
import com.c202.user.auth.jwt.TokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    UserResponseDto register(SignupRequestDto request);

    TokenDto.TokenResponseDto login(LoginRequestDto request);

    void logout(HttpServletRequest request, HttpServletResponse response, Integer userSeq);


    boolean isUsernameAvailable(String username);

    boolean isNicknameAvailable(String nickname);
}
