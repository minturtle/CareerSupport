package org.minturtle.careersupport.user.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.minturtle.careersupport.user.entity.User;

@Getter
@AllArgsConstructor
public class UserLoginResponse {
    private final String nickname;
    private final String token;


    public static UserLoginResponse of(User user, String token){
        return new UserLoginResponse(user.getNickname(), token);
    }
}
