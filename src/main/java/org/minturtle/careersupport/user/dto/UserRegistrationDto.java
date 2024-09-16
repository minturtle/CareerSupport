package org.minturtle.careersupport.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UserRegistrationDto {
    private String nickname;
    private String username;
    private String password;

}
