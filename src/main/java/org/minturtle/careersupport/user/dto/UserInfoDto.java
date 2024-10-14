package org.minturtle.careersupport.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.minturtle.careersupport.user.entity.User;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UserInfoDto {

    private String id;
    private String nickname;
    private String username;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        UserInfoDto that = (UserInfoDto) object;
        return Objects.equals(id, that.id) && Objects.equals(nickname, that.nickname) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nickname, username);
    }
}