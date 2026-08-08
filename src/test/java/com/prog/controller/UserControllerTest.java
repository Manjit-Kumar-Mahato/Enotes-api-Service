package com.prog.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.prog.dto.PasswordChngRequest;
import com.prog.dto.UserResponse;
import com.prog.entity.User;
import com.prog.service.UserService;
import com.prog.util.CommonUtil;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ModelMapper mapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testGetProfile() throws Exception {

        User user = mock(User.class);
        UserResponse userResponse = mock(UserResponse.class);

        when(mapper.map(user, UserResponse.class)).thenReturn(userResponse);

        try (MockedStatic<CommonUtil> mockedCommonUtil = CommonUtilMock(user)) {

            mockMvc.perform(
                    get("/api/v1/user/profile"))
                    .andExpect(status().isOk());

            verify(mapper).map(user, UserResponse.class);
        }
    }

    @Test
    public void testChangePassword() throws Exception {

        mockMvc.perform(
                post("/api/v1/user/chng-pswd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "oldPassword": "oldPassword",
                                    "newPassword": "newPassword"
                                }
                                """))
                .andExpect(status().isOk());
        verify(userService).changePassword(org.mockito.ArgumentMatchers.any(PasswordChngRequest.class));
    }

    private MockedStatic<CommonUtil> CommonUtilMock(User user) {
        MockedStatic<CommonUtil> mockedCommonUtil = org.mockito.Mockito.mockStatic(CommonUtil.class);
        mockedCommonUtil.when(CommonUtil::getLoggedInUser).thenReturn(user);
        return mockedCommonUtil;
    }
}