package org.meltzg.edhd.security;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@PasswordMatches
public class UserDTO {
    @ValidEmail
    @NotNull
    @NotEmpty
    private String username; // email

    @NotNull
    @NotEmpty
    private String password;
    private String matchingPassword;

    public UserDTO(String username, String password, String matchingPassword) {
        super();
        this.username = username;
        this.password = password;
        this.matchingPassword = matchingPassword;
    }

    public UserDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMatchingPassword() {
        return matchingPassword;
    }

    public void setMatchingPassword(String matchingPassword) {
        this.matchingPassword = matchingPassword;
    }
}
