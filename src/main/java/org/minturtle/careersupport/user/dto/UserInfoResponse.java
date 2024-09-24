package org.minturtle.careersupport.user.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.minturtle.careersupport.user.entity.User;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class UserInfoResponse {

    private String nickname;

    public static UserInfoResponse of(User user){
        return new UserInfoResponse(user.getNickname());
    }

    public static UserInfoResponse of(UserInfoDto user){
        return new UserInfoResponse(user.getNickname());
    }
}