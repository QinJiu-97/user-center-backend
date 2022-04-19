package com.qinjiu.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;
import java.rmi.server.UID;
import java.util.UUID;

/**
 * 用户登陆请求体
 *
 * @author QinJiu
 * @date 2022/3/20
 */
@Data
public class UserLoginRequest implements Serializable {


    private static final long serialVersionUID = -1204518942900220975L;
    private String userAccount;
    private String userPassword;

}
