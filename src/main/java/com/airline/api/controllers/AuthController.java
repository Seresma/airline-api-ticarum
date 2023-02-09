package com.airline.api.controllers;

import com.airline.api.auth.dto.LoginDto;
import com.airline.api.auth.dto.SignupDto;
import com.airline.api.auth.dto.UserJwtDto;
import com.airline.api.auth.services.UserServiceImpl;
import com.airline.api.context.GlobalConfig;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@Api(description = "Provides endpoints to authenticate users" )
@RequestMapping(GlobalConfig.AIRLINE_NAME + "/auth")
public class AuthController {

  private final UserServiceImpl userService;

  @ApiOperation(value = "Logs user into the system", response = UserJwtDto.class)
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Successful operation"),
          @ApiResponse(code = 400, message = "Invalid username/password supplied"),
          @ApiResponse(code = 401, message = "Bad credentials"),
          @ApiResponse(code = 404, message = "User not found"),
          @ApiResponse(code = 500, message = "Internal Server Error")
  })
  @PostMapping("/login")
  public UserJwtDto authenticateUser(@ApiParam(value = "User credentials", required = true) @Valid @RequestBody LoginDto login) {
    return this.userService.authenticateUser(login);
  }

  @ApiOperation(value = "Registers a new user into the system")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Successful operation"),
          @ApiResponse(code = 400, message = "Invalid data supplied"),
          @ApiResponse(code = 500, message = "Internal Server Error")
  })
  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@ApiParam(value = "User registration data", required = true) @Valid @RequestBody SignupDto signup) {
    this.userService.registerUser(signup);
    return ResponseEntity.ok("User registered successfully!");
  }
}
