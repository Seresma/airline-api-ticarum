package com.airline.api.controllers;

import com.airline.api.auth.dto.LoginDto;
import com.airline.api.auth.dto.UserJwtDto;
import com.airline.api.auth.services.UserServiceImpl;
import com.airline.api.context.GlobalConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthorizedAirlineControllerTest {

    // !!! IMPORTANT -> GlobalConfig.IS_AUTHENTICATION_ENABLE and GlobalConfig.IS_DATA_INITIALIZATION_ENABLE both must be TRUE
    private final String baseUri = "/" + GlobalConfig.AIRLINE_NAME;

    @Autowired
    private MockMvc mockMvcAuthorizedAirlineController;
    @Autowired
    private UserServiceImpl userService;
    private String userJwtToken;
    private String adminJwtToken;

    @Before
    public void setUp() {
        UserJwtDto userJwtDto = this.userService.authenticateUser(new LoginDto("user", "password"));
        userJwtToken = userJwtDto.getToken();

        UserJwtDto adminJwtDto = this.userService.authenticateUser(new LoginDto("admin", "password"));
        adminJwtToken = adminJwtDto.getToken();
    }

    @Test
    public void whenGetAirlineInfoNotAuthenticated_thenUnauthorized() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/info")).andExpect(status().isUnauthorized())
                .andExpect((jsonPath("$.message").value("Full authentication is required to access this resource")));
    }

    @Test
    public void whenGetAirlineInfoUserAuthorized_thenOk() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/info")
                        .header("Authorization", "Bearer " + userJwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void whenGetAirlineInfoAdminAuthorized_thenOk() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/info")
                        .header("Authorization", "Bearer " + adminJwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void whenFindAllPendingFlightsNotAuthenticated_thenUnauthorized() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/vuelo")).andExpect(status().isUnauthorized())
                .andExpect((jsonPath("$.message").value("Full authentication is required to access this resource")));
    }

    @Test
    public void whenFindAllPendingFlightsUserAuthorized_thenOk() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/vuelo")
                        .header("Authorization", "Bearer " + userJwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void whenFindAllPendingFlightsAdminAuthorized_thenOk() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/vuelo")
                        .header("Authorization", "Bearer " + adminJwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void whenAddFlightNotAuthenticated_thenUnauthorized() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(post(this.baseUri + "/vuelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect((jsonPath("$.message").value("Full authentication is required to access this resource")));
    }

    @Test
    public void whenAddFlightUserAuthorized_thenForbidden() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(post(this.baseUri + "/vuelo")
                        .header("Authorization", "Bearer " + userJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenAddFlightAdminAuthorized_thenCreated() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(post(this.baseUri + "/vuelo")
                        .header("Authorization", "Bearer " + adminJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void whenGetFlightByIdNotAuthenticated_thenUnauthorized() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/vuelo" + "/1")).andExpect(status().isUnauthorized())
                .andExpect((jsonPath("$.message").value("Full authentication is required to access this resource")));
    }

    @Test
    public void whenGetFlightByIdFlightsUserAuthorized_thenOk() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/vuelo" + "/1")
                        .header("Authorization", "Bearer " + userJwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void whenGetFlightByIdFlightsAdminAuthorized_thenOk() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/vuelo" + "/1")
                        .header("Authorization", "Bearer " + adminJwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void whenUpdateFlightByIdNotAuthenticated_thenUnauthorized() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(put(this.baseUri + "/vuelo" + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect((jsonPath("$.message").value("Full authentication is required to access this resource")));
    }

    @Test
    public void whenUpdateFlightByIdUserAuthorized_thenForbidden() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(put(this.baseUri + "/vuelo" + "/1")
                        .header("Authorization", "Bearer " + userJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenUpdateFlightByIdAdminAuthorized_thenNoContent() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(put(this.baseUri + "/vuelo" + "/1")
                        .header("Authorization", "Bearer " + adminJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void whenDeleteFlightByIdNotAuthenticated_thenUnauthorized() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(delete(this.baseUri + "/vuelo" + "/3")).andExpect(status().isUnauthorized())
                .andExpect((jsonPath("$.message").value("Full authentication is required to access this resource")));
    }

    @Test
    public void whenDeleteFlightByIdFlightsUserAuthorized_thenForbidden() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(delete(this.baseUri + "/vuelo" + "/3")
                        .header("Authorization", "Bearer " + userJwtToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenDeleteFlightByIdFlightsAdminAuthorized_thenNoContent() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(delete(this.baseUri + "/vuelo" + "/3")
                        .header("Authorization", "Bearer " + adminJwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void whenFindAllDepartedFlightsNotAuthenticated_thenUnauthorized() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/salida")).andExpect(status().isUnauthorized())
                .andExpect((jsonPath("$.message").value("Full authentication is required to access this resource")));
    }

    @Test
    public void whenFindAllDepartedFlightsUserAuthorized_thenOk() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/salida")
                        .header("Authorization", "Bearer " + userJwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void whenFindAllDepartedFlightsAdminAuthorized_thenOk() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/salida")
                        .header("Authorization", "Bearer " + adminJwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void whenGetFlightStatusByIdNotAuthenticated_thenUnauthorized() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/salida" + "/1")).andExpect(status().isUnauthorized())
                .andExpect((jsonPath("$.message").value("Full authentication is required to access this resource")));
    }

    @Test
    public void whenGetFlightStatusByIdFlightsUserAuthorized_thenOk() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/salida" + "/1")
                        .header("Authorization", "Bearer " + userJwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void whenGetFlightStatusByIdFlightsAdminAuthorized_thenOk() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(get(this.baseUri + "/salida" + "/1")
                        .header("Authorization", "Bearer " + adminJwtToken))
                .andExpect(status().isOk());
    }

    @Test
    public void whenDepartFlightByIdNotAuthenticated_thenUnauthorized() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(put(this.baseUri + "/salida" + "/1" + "/despegue"))
                .andExpect(status().isUnauthorized())
                .andExpect((jsonPath("$.message").value("Full authentication is required to access this resource")));
    }

    @Test
    public void whenDepartFlightByIdUserAuthorized_thenForbidden() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(put(this.baseUri + "/salida" + "/1" + "/despegue")
                        .header("Authorization", "Bearer " + userJwtToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenDepartFlightByIdAdminAuthorized_thenNoContent() throws Exception {
        this.mockMvcAuthorizedAirlineController.perform(put(this.baseUri + "/salida" + "/1" + "/despegue")
                        .header("Authorization", "Bearer " + adminJwtToken))
                .andExpect(status().isNoContent());
    }
}
