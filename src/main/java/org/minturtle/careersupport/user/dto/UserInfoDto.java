package org.minturtle.careersupport.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.minturtle.careersupport.user.entity.User;

@AllArgsConstructor
@Getter
@Builder
public class UserInfoDto {

    private final String id;
    private final String nickname;
    private final String username;

    public static UserInfoDto of(Object user){
        if (user instanceof User u) {
            return new UserInfoDto(
                    u.getId(),
                    u.getNickname(),
                    u.getUsername()
            );
        }

        throw new IllegalStateException("Unexpected object type: " + user.getClass().getName());


    }

}
