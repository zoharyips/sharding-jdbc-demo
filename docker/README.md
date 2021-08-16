1. 启动容器 `docker compose up -d`;
2. 登录 Master `mysql -uroot -h 127.0.0.1 -P 3307 -p` ;
3. 查看 master 状态。
   ~~~
   mysql> show master status\G
   *************************** 1. row ***************************
   File: mysql-bin.000004
   Position: 156
   Binlog_Do_DB: db_order_1,db_order_2,db_user
   Binlog_Ignore_DB:
   Executed_Gtid_Set:
   1 row in set (0.01 sec)
   ~~~
4. 登录 Slave `mysql -uroot -h 127.0.0.1 -P 3308 -p`
5. 设置 Master 连接
   ~~~
   mysql> change master to master_host='172.25.0.101',master_user='root',master_password='123456',master_port=3306,master_log_file='mysql-bin.000004',master_log_pos=156;
   Query OK, 0 rows affected, 8 warnings (0.08 sec)
   
   mysql> start slave;
   Query OK, 0 rows affected, 1 warning (0.00 sec)
   
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