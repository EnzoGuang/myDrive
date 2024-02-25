# 创建用户表
create table user(
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255) COMMENT '使用md5算法进行加密可以考虑密码加盐',
    username VARCHAR(50),
    admin BOOLEAN DEFAULT 0 COMMENT '0不是管理员1是管理员'
);

# 创建文件表
create table file(
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255),
    file_hash VARCHAR(255) COMMENT '存储文件的SHA-256的hash值',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小',
    total_chunks INT UNSIGNED COMMENT '表示该文件由多少个分片组成',
    folder_id INT COMMENT '外键关联folder表,表示当前文件所属哪个文件夹下,若为null则该文件属于根目录',
    user_id INT COMMENT '外键关联user表,表示当前文件属于哪个用户',
    version_control_enabled BOOLEAN DEFAULT FALSE COMMENT '标记文件是否开启版本管理',
    current_version_id INT DEFAULT NULL COMMENT '表示当前使用的版本',
    upload_time DATETIME,
    status VARCHAR(255) DEFAULT 'active' COMMENT '标识文件状态，active为正常，deleted为已删除进入回收站',
    delete_time DATETIME DEFAULT NULL COMMENT '记录文件被删除的时间',
    FOREIGN KEY (folder_id) REFERENCES folder(id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (current_version_id) REFERENCES file_version(id)
);

# 创建分片表
create table chunk(
    id INT PRIMARY KEY AUTO_INCREMENT,
    chunk_hash VARCHAR(255) UNIQUE COMMENT '该分片的hash值,hash值同时作为存储路径',
    reference_count INT UNSIGNED DEFAULT 1 COMMENT '该分片被引用的次数, 不同文件可能有相同hash值的分片',
    chunk_size BIGINT COMMENT '分片的大小',
    upload_time DATETIME
);

# 创建文件分片关系表
create table file_chunk(
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_id INT COMMENT '外键关联file表',
    chunk_id INT COMMENT '外键关联chunk表',
    chunk_index INT NOT NULL COMMENT '文件分片序号',
    version_id INT NULL COMMENT '关联具体的文件版本',
    upload_time DATETIME,
    FOREIGN KEY (file_id) REFERENCES file(id),
    FOREIGN KEY (chunk_id) REFERENCES chunk(id),
    FOREIGN KEY (version_id) REFERENCES file_version(id)
);

# 创建版本控制表
create table file_version(
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_id INT NOT NULL,
    version_number INT UNSIGNED DEFAULT 0 NOT NULL COMMENT '版本序号',
    message VARCHAR(255) COMMENT '备注信息',
    upload_time DATETIME
);
# 添加file_version的外键
ALTER TABLE file_version ADD FOREIGN KEY (file_id) REFERENCES file(id);

# 创建文件夹表
create table folder(
    id INT PRIMARY KEY AUTO_INCREMENT,
    folder_name VARCHAR(255),
    parent_folder_id INT DEFAULT NULL,
    user_id INT COMMENT '外键关联user表',
    create_time DATETIME,
    status VARCHAR(255) DEFAULT 'active' COMMENT '标识文件夹状态，active为正常，deleted为已删除进入回收站',
    delete_time DATETIME DEFAULT NULL COMMENT '记录文件夹被删除的时间',
    FOREIGN KEY (user_id) REFERENCES user(id)
);