package com.cms.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RegisterDTO {
    @NotBlank private String username;
    @NotBlank @Size(min = 6, max = 20) private String password;
    private String nickname;
    private String realName;
    private String college;
    private String phone;
}
