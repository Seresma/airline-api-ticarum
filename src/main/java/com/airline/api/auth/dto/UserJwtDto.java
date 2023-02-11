package com.airline.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserJwtDto {
    private String token;
    private String type;
    private Long id;
    private String username;
    private String email;
    private String rol;
}
