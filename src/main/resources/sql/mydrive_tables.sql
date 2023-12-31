create table user(
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(32) COMMENT '使用md5算法进行加密可以考虑密码加盐',
    username VARCHAR(50),
    admin BOOLEAN DEFAULT 0 COMMENT '0不是管理员1是管理员'
);

# 创建文件表
create table file(
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255),
    hash_value VARCHAR(255) COMMENT '存储文件的SHA-256的hash值',
    folder_id INT COMMENT '外键关联folder表,表示当前文件所属哪个文件夹下,若为null则该文件属于根目录',
    user_id INT COMMENT '外键关联user表,表示当前文件属于哪个用户',
    file_path VARCHAR(255),
    upload_time DATETIME,
    foreign key (folder_id) REFERENCES folder(id),
    Foreign Key (user_id) REFERENCES user(id)
);

# 创建文件夹表
create table folder(
    id INT PRIMARY KEY AUTO_INCREMENT,
    folder_name VARCHAR(255),
    parent_folder_id INT,
    user_id INT COMMENT '外键关联user表',
    create_time DATETIME,
    Foreign Key (user_id) REFERENCES user(id)
);
