package com.airline.api.auth.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class User {
  @Id
  @GeneratedValue
  private Long id;
  @NotBlank
  private String username;
  @NotBlank
  private String password;
  @NotBlank
  @Email
  private String email;
  private Role role;

  public User(String username, String password, String email, Role rol) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.role = rol;
  }
}
