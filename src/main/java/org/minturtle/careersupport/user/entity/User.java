package org.minturtle.careersupport.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class User {

    @Id
    private String id;

    private String nickname;

    @Indexed(unique = true)
    private String username;

    private String password;

    private String apiToken;

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
}