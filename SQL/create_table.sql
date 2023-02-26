-- 用户表
create table user
(
    id           bigint auto_increment comment '用户id'
        primary key,
    username     varchar(64)                         null comment '用户名',
    userAccount  varchar(128)                        not null comment '用户账号',
    userPassword varchar(128)                        not null comment '用户密码',
    avatarUrl    varchar(256)                        null comment '用户头像',
    user_profile varchar(512)                        null comment '个人简介',
    gender       tinyint   default 1                 not null comment '用户性别 1 - 男，0 - 女',
    Email        varchar(128)                        null comment '用户邮箱
',
    userStatus   int                                 null comment '用户状态',
    phone        varchar(128)                        null comment '用户电话',
    updateTime   timestamp default CURRENT_TIMESTAMP null comment '更新时间',
    isDelete     tinyint   default 0                 null comment '是否删除： 0 - 未删除，1 - 删除',
    createTime   timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    planetCode   varchar(512)                        null comment '星球编号',
    tags         varchar(1024)                       null comment '标签',
    userRole     tinyint   default 0                 null comment '用户角色 0 - 普通用户， 1 - 管理员',
    constraint user_planetCode_uindex
        unique (planetCode),
    constraint user_userAccount_uindex
        unique (userAccount)
);

-- 队伍表
create table team
(
    teamId       bigint auto_increment comment '队伍id'
        primary key,
    teamName     varchar(256) default '新建队伍'            not null comment '队伍名称
',
    teamDesc     varchar(512)                           null comment '队伍描述',
    userId       bigint                                 null comment '创建人id',
    maxNum       int          default 1                 not null comment '最大人数',
    expireTime   datetime                               null comment '过期时间',
    teamPassword varchar(128)                           null comment '队伍密码',
    teamStatus   int          default 0                 not null comment '队伍状态：0-公开，1-私有，2-加密',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '队伍创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null comment '队伍更新时间',
    isDelete     int          default 0                 not null comment '是否删除：0-未删，1-已删',
    constraint Team_teamId_uindex
        unique (teamId)
)
    comment '队伍表';

-- 标签表
create table tag
(
    id         bigint auto_increment comment '用户id'
        primary key,
    tagName    varchar(64)                         null comment '标签名',
    userId     bigint                              null comment '用户id',
    parentId   bigint                              null comment '父标签id',
    isParent   tinyint   default 1                 null comment '0- 不是，1- 父标签',
    createTime timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime timestamp default CURRENT_TIMESTAMP null comment '更新时间',
    isDelete   tinyint   default 0                 null comment '是否删除： 0 - 未删除，1 - 删除',
    tags       varchar(1024)                       null comment '标签',
    constraint UniIdx_tagName
        unique (tagName)
)
    comment '标签';

create index Idx_UserId
    on tag (userId);

-- 用户队伍关系表
create table user_team
(
    id         bigint auto_increment comment 'id
'
        primary key,
    userId     varchar(512)                       not null comment '用户id',
    teamId     varchar(512)                       not null comment '队伍id',
    joinTime   datetime                           null comment '加入时间',
    crateTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete   int      default 0                 not null comment '是否删除：0-未删，1-已删',
    constraint user_team_id_uindex
        unique (id)
)
    comment '用户队伍关系表';
