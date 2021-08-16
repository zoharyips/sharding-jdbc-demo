# sharding-jdbc-demo
Sharding-JDBC 是无侵入式的 MySQL 分库分表操作工具，所有库表设置仅需要在配置文件中配置即可，无须修改任何代码。

本文写了一个 Demo，使用的是 SpringBoot 框架，通过 Docker 进行 MySQL 实例管理，分库分表结构如下图，同时所有的库都进行了主从复制：

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/aba5b869f1c34096b7946d3cdafbcb19~tplv-k3u1fbpfcp-watermark.image)

### 主从库搭建

Docker 项目结构：

~~~
docker
├── docker-compose.yml
├── master
│   ├── data
│   ├── log
│   │   └── error.log
│   ├── my.cnf
│   └── mysql-files  # Win 需要，Linux 不需要
├── README.md
└── slave
    ├── data
    ├── log
    │   └── error.log
    ├── my.cnf
    └── mysql-files
~~~

#### Compose File

~~~docker
version: '3'

networks:
  sharding-jdbc-demo:
    driver: bridge
    ipam:
      config:
        - subnet: 172.25.0.0/24

services:
  master:
    image: mysql
    container_name: sharding-jdbc-demo-master
    ports:
      - "3307:3306"
    volumes:
      - "./master/data:/var/lib/mysql"
      - "./master/mysql-files:/var/lib/mysql-files"         # win 下的 MySQL8 需要，Linux 不需要
      - "./master/log/error.log:/var/log/mysql/error.log"
      - "./master/my.cnf:/etc/mysql/my.cnf"
    environment:
      MYSQL_ROOT_PASSWORD: 123456
    entrypoint: bash -c "chown -R mysql:mysql /var/log/mysql && chmod 644 /etc/mysql/my.cnf && exec /entrypoint.sh mysqld"
    restart: unless-stopped
    networks:
      sharding-jdbc-demo:
        ipv4_address: 172.25.0.101

  slave:
    image: mysql
    container_name: sharding-jdbc-demo-slave
    ports:
      - "3308:3306"
    volumes:
      - "./slave/data:/var/lib/mysql"
      - "./slave/mysql-files:/var/lib/mysql-files"
      - "./slave/log/error.log:/var/log/mysql/error.log"
      - "./slave/my.cnf:/etc/mysql/my.cnf"
    environment:
      MYSQL_ROOT_PASSWORD: 123456
    entrypoint: bash -c "chown -R mysql:mysql /var/log/mysql && chmod 644 /etc/mysql/my.cnf  && exec /entrypoint.sh mysqld"
    restart: unless-stopped
    networks:
      sharding-jdbc-demo:
        ipv4_address: 172.25.0.102
~~~

#### Master 配置

~~~
[mysqld]
pid-file         = /var/run/mysqld/mysqld.pid
socket           = /var/run/mysqld/mysqld.sock
datadir          = /var/lib/mysql
log-error        = /var/log/mysql/error.log
bind-address     = 0.0.0.0
secure-file-priv = NULL
max_connections  = 16384

character-set-server = utf8mb4
collation-server     = utf8mb4_general_ci
init_connect         ='SET NAMES utf8mb4'
skip-name-resolve

server_id = 1
log-bin = mysql-bin
binlog-do-db = db_order_1 # 复制 db_order_1
binlog-do-db = db_order_2 # 复制 db_order_2
binlog-do-db = db_user    # 复制 db_user
log-slave-updates
sync_binlog = 1
auto_increment_offset = 1
auto_increment_increment = 1
expire_logs_days = 7
log_bin_trust_function_creators = 1

# Custom config should go here
!includedir /etc/mysql/conf.d/
~~~

#### Slave 配置

~~~
[mysqld]
pid-file         = /var/run/mysqld/mysqld.pid
socket           = /var/run/mysqld/mysqld.sock
datadir          = /var/lib/mysql
log-error        = /var/log/mysql/error.log
bind-address     = 0.0.0.0
secure-file-priv = NULL
max_connections  = 16384

character-set-server = utf8mb4
collation-server     = utf8mb4_general_ci
init_connect         ='SET NAMES utf8mb4'
skip-name-resolve
skip-host-cache

server_id = 2
log-bin = mysql-bin
log-slave-updates
sync_binlog = 0
innodb_flush_log_at_trx_commit = 0  # 提交策略
replicate-do-db = db_order_1        # 复制 db_order_1
replicate-do-db = db_order_2        # 复制 db_order_2
replicate-do-db = db_user           # 复制 db_user
slave-net-timeout = 60              # 重连时间
log_bin_trust_function_creators = 1

# Custom config should go here
!includedir /etc/mysql/conf.d/
~~~

#### 主从配置

1. 启动容器 `docker compose up -d`;
2. 登录 Master `mysql -uroot -h 127.0.0.1 -P 3307 -p` ;
3. 查看 master 状态。
   ~~~
   mysql> show master status\G
   *************************** 1. row ***************************
   File: mysql-bin.000004                            # 记住 Bin log 当前文件名称
   Position: 156                                     # 记住 Bin log 当前偏移量
   Binlog_Do_DB: db_order_1,db_order_2,db_user       # 确认复制数据库是否正确
   Binlog_Ignore_DB:
   Executed_Gtid_Set:
   ~~~
4. 登录 Slave `mysql -uroot -h 127.0.0.1 -P 3308 -p`
5. 设置 Master 连接，注意 `host` 与 `port` 是内网的地址和端口。
   ~~~
   mysql> change master to master_host='172.25.0.101',
       master_user='root',
       master_password='123456',
       master_port=3306,
       master_log_file='mysql-bin.000004',
       master_log_pos=156;
   ~~~
6. 启动同步
   ~~~ mysql
   mysql> start slave;
   ~~~
7. 查看 Slave 状态，若 `Slave_IO` 与 `Slave_SQL` 都在运行为 `YES` 即成功。
   ~~~ mysql
   mysql> show slave status\G
   *************************** 1. row ***************************
   Slave_IO_State: Waiting for source to send event
   Master_Host: 172.25.0.101
   Master_User: root
   Master_Port: 3306
   Connect_Retry: 60
   Master_Log_File: mysql-bin.000004
   Read_Master_Log_Pos: 156
   Relay_Log_File: d2a706a02933-relay-bin.000002
   Relay_Log_Pos: 324
   Relay_Master_Log_File: mysql-bin.000004
   Slave_IO_Running: Yes
   Slave_SQL_Running: Yes
   Replicate_Do_DB: db_order_1,db_order_2,db_user
   ~~~
   
### 创建分库分表

登录 Master，创建数据库：

~~~sql
CREATE DATABASE db_order_1;
CREATE DATABASE db_order_2;
CREATE DATABASE db_user;
~~~

此时从库也会创建数据库，若没有，则是主从配置失败了。

此时已完成垂直分库和水平分库。接下来创建数据表：

#### Order 1 库

先 `USE db_order_1;`，再分别创建 `t_dict` 全局表、`t_order_1` 和 `t_order_2` 水平分表。

~~~sql
DROP TABLE IF EXISTS `t_dict`;
CREATE TABLE `t_dict`
(
    `id`         int                                    NOT NULL AUTO_INCREMENT,
    `type`       int                                    NOT NULL,
    `enum_value` int                                    NOT NULL,
    `name`       varchar(64) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
LOCK TABLES `t_dict` WRITE;
INSERT INTO `t_dict` VALUES (1,1,0,'未定义'),(2,1,1,'未付款'),(3,1,2,'已付款'),(4,1,3,'退款中'),(5,1,4,'已退款'),(6,1,5,'已完成'),(7,2,0,'未定义'),(8,2,1,'已创建'),(9,2,2,'已验证'),(10,2,3,'已冻结'),(11,2,4,'已注销'),(12,2,5,'已删除');
UNLOCK TABLES;

DROP TABLE IF EXISTS `t_order_1`;
CREATE TABLE `t_order_1`
(
    `id`      bigint         NOT NULL,
    `user_id` bigint         NOT NULL,
    `price`   decimal(10, 2) NOT NULL,
    `status`  int            NOT NULL DEFAULT '1',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

DROP TABLE IF EXISTS `t_order_2`;
CREATE TABLE `t_order_2`
(
    `id`      bigint         NOT NULL,
    `user_id` bigint         NOT NULL,
    `price`   decimal(10, 2) NOT NULL,
    `status`  int            NOT NULL DEFAULT '1',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
~~~

#### Order 2 库

先 `USE db_order_2;`，再分别创建 `t_dict` 全局表、`t_order_1` 和 `t_order_2` 水平分表。所执行 SQL 与 `db_order_1` 一致。

#### User 库

先 `USE db_user;`，再分别创建 `t_dict` 全局表和 `t_user` 表，此处就不进行水平或垂直分表了。垂直分表 `sharding-jdbc` 不会去处理，因为垂直分表之后就是**异表异构**了，执行 Join 操作就可以了，或者代码进行多次查询实现。

~~~sql
DROP TABLE IF EXISTS `t_dict`;
CREATE TABLE `t_dict`
(
    `id`         int                                    NOT NULL AUTO_INCREMENT,
    `type`       int                                    NOT NULL,
    `enum_value` int                                    NOT NULL,
    `name`       varchar(64) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
LOCK TABLES `t_dict` WRITE;
INSERT INTO `t_dict` VALUES (1,1,0,'未定义'),(2,1,1,'未付款'),(3,1,2,'已付款'),(4,1,3,'退款中'),(5,1,4,'已退款'),(6,1,5,'已完成'),(7,2,0,'未定义'),(8,2,1,'已创建'),(9,2,2,'已验证'),(10,2,3,'已冻结'),(11,2,4,'已注销'),(12,2,5,'已删除');
UNLOCK TABLES;


DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`
(
    `id`   bigint                                  NOT NULL AUTO_INCREMENT,
    `name` varchar(128) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
    `type` int                                     NOT NULL DEFAULT '1',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1426999086541635586
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
~~~

### Sharding-JDBC 引入

Sharding-JDBC maven 包：

~~~xml
<!-- Sharding-jdbc -->
<dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
        <version>4.1.1</version>
</dependency>
~~~

本 Demo 其他用到的依赖，分别是 Junit 测试、Lombok、MyBatis Plus、Druid 连接池、MySQL 驱动、Java Faker 数据生成器：

~~~xml
<dependencies>
        <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-test</artifactId>
                <scope>test</scope>
        </dependency>
        <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>4.13.2</version>
                <scope>test</scope>
        </dependency>
        <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <optional>true</optional>
        </dependency>
        <!-- MyBatis Plus -->
        <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-boot-starter</artifactId>
                <version>3.4.3.1</version>
        </dependency>
        <!-- Druid -->
        <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>druid</artifactId>
                <version>1.2.6</version>
        </dependency>
        <!-- MySQL -->
        <dependency>
                <groupId>mysql</groupId>
                <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <!-- Sharding-jdbc -->
        <dependency>
                <groupId>org.apache.shardingsphere</groupId>
                <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
                <version>4.1.1</version>
        </dependency>
        <!-- Data Faker -->
        <dependency>
                <groupId>com.github.javafaker</groupId>
                <artifactId>javafaker</artifactId>
                <version>1.0.2</version>
        </dependency>
</dependencies>
~~~

### Sharding-JDBC 配置

#### 可选配置

* 启用 SQL 打印：

    ~~~properties
    spring.shardingsphere.props.sql.show = true
    ~~~

#### 数据源配置

总共有 `t_order_1`、`t_order_2` 和 `t_user` 三个库，加上单主单从的复制，因此有 6 个数据库，需要配置六个数据源：

~~~properties
# Datasource Define
spring.shardingsphere.datasource.names = o1-master,o2-master,o1-slave,o2-slave,u-master,u-slave
# datasource o1-master
spring.shardingsphere.datasource.o1-master.type = com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.o1-master.driver-class-name = com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.o1-master.url = jdbc:mysql://localhost:3307/db_order_1?useUnicode=true
spring.shardingsphere.datasource.o1-master.username = root
spring.shardingsphere.datasource.o1-master.password = 123456
# datasource o1-slave
spring.shardingsphere.datasource.o1-slave.type = com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.o1-slave.driver-class-name = com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.o1-slave.url = jdbc:mysql://localhost:3308/db_order_1?useUnicode=true
spring.shardingsphere.datasource.o1-slave.username = root
spring.shardingsphere.datasource.o1-slave.password = 123456
# datasource o2-master
spring.shardingsphere.datasource.o2-master.type = com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.o2-master.driver-class-name = com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.o2-master.url = jdbc:mysql://localhost:3307/db_order_2?useUnicode=true
spring.shardingsphere.datasource.o2-master.username = root
spring.shardingsphere.datasource.o2-master.password = 123456
# datasource o2-slave
spring.shardingsphere.datasource.o2-slave.type = com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.o2-slave.driver-class-name = com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.o2-slave.url = jdbc:mysql://localhost:3308/db_order_2?useUnicode=true
spring.shardingsphere.datasource.o2-slave.username = root
spring.shardingsphere.datasource.o2-slave.password = 123456
# datasource u-master
spring.shardingsphere.datasource.u-master.type = com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.u-master.driver-class-name = com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.u-master.url = jdbc:mysql://localhost:3307/db_user?useUnicode=true
spring.shardingsphere.datasource.u-master.username = root
spring.shardingsphere.datasource.u-master.password = 123456
# datasource u-slave
spring.shardingsphere.datasource.u-slave.type = com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.u-slave.driver-class-name = com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.u-slave.url = jdbc:mysql://localhost:3308/db_user?useUnicode=true
spring.shardingsphere.datasource.u-slave.username = root
spring.shardingsphere.datasource.u-slave.password = 123456
~~~

#### 主从复制配置

主从配置不需要声明，在定义时会自动读取 key 中的主从配置库作为逻辑库，如下面的 `db-order-1`。

~~~properties
# Replication Define
spring.shardingsphere.sharding.master-slave-rules.db-order-1.master-data-source-name=o1-master
spring.shardingsphere.sharding.master-slave-rules.db-order-1.slave-data-source-names=o1-slave
spring.shardingsphere.sharding.master-slave-rules.db-order-2.master-data-source-name=o2-master
spring.shardingsphere.sharding.master-slave-rules.db-order-2.slave-data-source-names=o2-slave
spring.shardingsphere.sharding.master-slave-rules.db-user.master-data-source-name=u-master
spring.shardingsphere.sharding.master-slave-rules.db-user.slave-data-source-names=u-slave
~~~

#### 数据节点配置

数据节点，指的是每张数据表，由于存在分库、分表、全局的不同类型，因此数据节点也有不同类型。注意，由于我们进行了主从复制，因此这里的数据库不能直接填数据源的名称，应该填在主从复制配置的 Key 中定义的名称，如 `db-user` 而不是 `u-master` 或 `u-slave`。

* **全局表**：

    ~~~properties
    # BroadCast Table
    spring.shardingsphere.sharding.broadcast-tables = t_dict
    ~~~
    
* **单库单表**：

    `key-generator.column` 设置主键列。  
    `key-generator.type` 设置主键生成类型，这里使用雪花算法，其实没必要因为不是分表的，但是不填也会默认使用这个。
    
    ~~~properties
    # Data Node t_user
    spring.shardingsphere.sharding.tables.t_user.actual-data-nodes = db-user.t_user
    spring.shardingsphere.sharding.tables.t_user.key-generator.column = id
    spring.shardingsphere.sharding.tables.t_user.key-generator.type = SNOWFLAKE
    ~~~

* **分库分表**：
    
    在 `actual-data-nodes` 中使用 groovy 表达式进行设置。  
    在 `database-strategy` 中设置切分方式，具体自查，暂没时间写。

    ~~~properties
    # Data Node t_order, If there is not master-salve-replication, use datasource name like "o$-master->{1..2}.t_order_$->{1..2}"
    spring.shardingsphere.sharding.tables.t_order.actual-data-nodes = db-order-$->{1..2}.t_order_$->{1..2}
    spring.shardingsphere.sharding.tables.t_order.key-generator.column = id
    spring.shardingsphere.sharding.tables.t_order.key-generator.type = SNOWFLAKE
    # database sharding strategy
    spring.shardingsphere.sharding.tables.t_order.database-strategy.inline.sharding-column = user_id
    spring.shardingsphere.sharding.tables.t_order.database-strategy.inline.algorithm-expression = db-order-$->{user_id % 2 + 1}
    # table sharding strategy
    spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.sharding-column = id
    spring.shardingsphere.sharding.tables.t_order.table-strategy.inline.algorithm-expression = t_order_$->{id % 2 + 1}
    ~~~

### Demo 程序

参见：[zoharyips/sharding-jdbc-demo (github.com)](https://github.com/zoharyips/sharding-jdbc-demo)
