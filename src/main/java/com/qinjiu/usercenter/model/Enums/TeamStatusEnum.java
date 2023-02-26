package com.qinjiu.usercenter.model.Enums;

/**
 * 队伍状态枚举
 *
 * @author QinJiu
 * @Date 2022/9/10
 */

public enum TeamStatusEnum {

    PUBLIC(0, "公共"),
    PRIVATE(1, "私有"),
    ENCRYPTION(2, "加密");

    private Integer value;

    private String text;

    TeamStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static TeamStatusEnum getEnumByValue(Integer value){
        if(value == null){
            return null;
        }
        TeamStatusEnum[] statusEnums = TeamStatusEnum.values();
        for (TeamStatusEnum statusEnum :statusEnums) {
            if(statusEnum.getValue() == value){
                return statusEnum;
            }
        }
        return null;

    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
