package com.qinjiu.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author QinJiu
 * @date 2022/3/20
 */
@Data
public class UserRegisterRequest implements Serializable {


   private String userAccount;
   private  String userPassword;
   private String checkPassword;
   private String planetCode;


}
