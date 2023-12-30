create table user(
                     id int primary key AUTO_INCREMENT,
                     user_name varchar(50),
                     password varchar(32) COMMENT '使用md5算法进行加密可以考虑密码加盐',
                     email varchar(100),
                     admin BOOLEAN DEFAULT 0 COMMENT '0不是管理员1是管理员'
);

# 创建文件表
create table file(
                     id int primary key,
                     file_name varchar(255),
                     file_path varchar(255),
                     user_id int COMMENT '外键关联user表',
                     upload_time DATETIME,
                     Foreign Key (user_id) REFERENCES user(id)
);

# 创建文件夹表
create table folder(
                       id int primary key,
                       foler_name varchar(255),
                       parent_folder_id int,
                       user_id int COMMENT '外键关联user表',
                       create_time datetime
);
