package org.minturtle.careersupport.user.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginResponse {
    private final String nickname;
    private final String token;
}
