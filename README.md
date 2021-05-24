## 博泰 canal 客户端 (pateo canal client)

### 介绍
canal 是阿里巴巴mysql数据库binlog的增量订阅&消费组件  
使用该客户端前请先了解canal,https://github.com/alibaba/canal
### 要求
java8+

### 特性
* 解耦单表增删操作
* kafka客户端支持
* 同步异步处理支持
* spring boot 项目简单配置即可配置多项目使用
*支持集群部署，高可用，高性能，高并发

### 一、如何使用
1.下载所有的源码
2.准备好zk,nacos,kafka，这里会用到nacos配置中心
3.启动入口在canal-client-server项目里面
4.在canal-client-server项目里面配置一下application.yml配置文件

###二、开始使用
####1.配置nacos中心配置新建一个主配置名字要跟项目里面配置的一样
```
pateo:
  canal: 
    instance:
      - shouchebao-canal:
        name: shouchebao-canal
        cluster: 1
      - shouchebao-canal-zzz:
        name: shouchebao-canal-zzz
        cluster: 1
    mode: kafka
    async: true
    kafka: kafka1.sit.ptcloud.t.home:9092,kafka2.sit.ptcloud.t.home:9092,kafka3.sit.ptcloud.t.home:9092
    zkServers: kafka1.uat.ptcloud.t.home:2181,kafka2.uat.ptcloud.t.home:2181,kafka3.uat.ptcloud.t.home:2181
    zkNode: /canalApp
```


|属性名|描述|是否必填(Y/N)|
|:----    |:---------------------    |:-----|
|- shouchebao-canal|无实际作用，区分项目业务自定义的名称，后面配置中会用到这个名称|Y|
|name|自定义的项目名称，后面配置中会用到这个名称，用于区分多项目配置用|Y|
|cluster|集群数，如果项目同步比较频繁可以多配置大一些|Y|
|mode|这里默认写 kafka|Y|
|async|是否异步|Y|
|kafka|kafka集群地址，用户订阅消息来同步用|Y|
|zkServers|zk集群地址|Y|
|zkNode|zk节点名称，默认为/canalApp,这里可以随便自定义不影响使用|Y|


###三、配置自己项目的配置文件
按上面instance，name中配置的项目名称，在nacos里面新建一个跟项目名称一样的.yaml配置文件，如上面定义的name是shouchebao-canal
```
- shouchebao-canal:
  name: shouchebao-canal
```
如这里定义的name是shouchebao-canal
那就需要新建一个配置文件：
```
Data ID：shouchebao-canal.yaml
Group：DEFAULT_GROUP
配置格式：YAML
```
###四、自己项目配置文件详解
####1、配置需要定义的kafka主题信息
```
shouchebao-canal: #需要跟配置文件名称一样
  consumers: 
    - consumer: 
      topic: retail_nuat_009_content_channel
      groupId: canal-client-content-channel
    - consumer1: 
      topic: retail_nuat_009_tenant_account_profile
      groupId: canal-client-tenant-account-profile
```
|属性名|描述|是否必填(Y/N)|
|:----    |:---------------------    |:-----|
|- consumer|业务名称随便定义，可以定义N个|Y|
|topic|kafka需要订阅的主题|Y|
|groupId|集群数，如果项目同步比较频繁可以多配置大一些|Y|

####2、配置数据源信息
```
shouchebao-canal:
  datasource: #数据源配置
    druid:
      # 数据库访问配置, 使用druid数据源
      # 数据源1 scb_customer
      scb-customer: #自定义数据源名称
        type: com.alibaba.druid.pool.DruidDataSource
        driverClassName: com.mysql.jdbc.Driver
        url: jdbc:mysql://mdb.sit.ptcloud.t.home:3306/scb_customer?allowMultiQueries=true&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: scb_customer
        password: JsnK1&8SKC
    # 数据源2 scb_content数据库
      scb-content:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.jdbc.Driver
        url: jdbc:mysql://mdb.sit.ptcloud.t.home:3306/scb_content?allowMultiQueries=true&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: scb_content
        password: SOejDw02vo

    # 连接池配置
    initialSize: 5
    # 最小连接数
    minIdle: 5
    # 最大连接数
    maxActive: 20
    # 连接等待超时时间
    maxWait: 30000
    # 配置检测可以关闭的空闲连接间隔时间
    timeBetweenEvictionRunsMillis: 3000
    # 配置连接在池中的最小生存时间
    minEvictableIdleTimeMillis: 30001
    #下面这两个配置，可以在每次连接的时候判断一些连接是否有效
    testOnBorrow: true
    testWhileIdle: true
```
####3、配置elasticsearch连接信息
```
shouchebao-canal:
  elasticsearch: 
    es1:
      esAddress: 172.16.42.15:8200
      connectTimeOut: 10000
      socketTimeOut: 9000
      connectionRequestTimeOut: 5000
      maxConnectNum: 30
      maxConnectPerRoute: 10
      keepAliveMinutes: 10
      username: xxxx
      password: xxxx
```

|属性名|描述|是否必填(Y/N)|
|:----    |:---------------------    |:-----|
|es1|自定义的es连接信息，可随意命名，下面配置es同步规则配置时，会用到这个名称|Y|
|esAddress|Es集群地址，多个用英文的,号分割如：172.16.42.15:8200, 172.16.42.16:8300|Y|
|groupId|集群数，如果项目同步比较频繁可以多配置大一些|Y|
|connectTimeOut|连接超时时间|Y|
|socketTimeOut|客户端从服务器读取数据的timeout|Y|
|connectionRequestTimeOut|获取连接的超时时间|Y|
|maxConnectNum|最大连接数|Y|
|maxConnectPerRoute|最大路由连接数|Y|
|keepAliveMinutes|keepAlive保活策略|Y|
|username|ES集群连接用户名|Y|
|password|ES集群连接密码|Y|

####4、(重点)es同步相关的配置
```
shouchebao-canal: 
###################################es同步相关配置############################################### 
  esSync: 
    scb_customer-customer_info: #数据库名称-表名 
      - customer-info1:  
        targetEs: es1 
        dbFields: sid,tenant_code,tenant_account_id,org_code,wx_open_id,channel_source,channel_source_name,follow_date,following_flag,channel_type,channel_value,version,create_by,create_date,update_by,update_date,customer_flag,follow_up_person,last_follow_time,next_follow_time,read_flag,last_result_sid,last_result_name,clue_level_sid,clue_level_name,phone,delete_flag,task_flag,follow_count,tenant_name,parent_tenant_code,tenant_flag #需要同步的字段数组格式 
        index: customer_info_index #es索引名称 
        indexDynamicFieldSuffix: tenant_code #索引后缀配置为字段名称 
        id: sid #es主键id 
        idPrefix: xxx #es主键id前缀 
        aviator表达式,tenant_code为上面fields定义的字段 
        syncRule: tenant_code != nil 
        #同步的事件，insert,update,delete,all,不配置默认为all 
        event: insert,update,delete 
        relations: 
          customer_join_info: 
            name: customer_info 
            #parent: customer_id 主表不用配置该属性 
        skips: #需要跳过不同步的字段 
          - tenant_code 
        esFieldTypes: #字段类型自定义，字段名称：类型，支持array,json,string,int,date 
          sid: int 
          tenant_account_id: int 
        esFields: #字段别名  数据库字段名称：es字段名称 
          org_code: orgCode
        updateArrayFields: #把指定字段插入到es某个数组里面，删除时需要删除数组里面数据,配置这个只会同步updateArrayFields里面定义的字段 
          - label_value_list: label_value_name #es数组字段：数据库字段
```
|属性名|描述|是否必填(Y/N)|
|:----    |:---------------------    |:-----|
|scb_customer-customer_info|此属性是用数据库名-表名拼接而成，可以配置多组，用于说明我要同步哪个库哪张表的数据到ES里面去|Y|
|- customer-info1|业务名称无实际作用，自己随便定义，用于区分这张表有几种同步到es里面去的规则，可以配置多组 - customer-info1下面所有的配置算一种同步规则|Y|
|targetEs|es连接源名称，是上面配置中的：elasticsearch下es连接名称|Y|
|dbFields|数据库表字段名称|Y|
|index|es索引名称，自定义任意字符串|Y|
|indexDynamicFieldSuffix|es索引动态后缀名，属性值配置dbFields里面的字段名称，这里会通过你配置的字段名去获取字段名的值|N|
|id|es主键，配置为dbFields字段名称|Y|
|idPrefix|es主键自定义前缀，值为自定义字符串|N|
|syncRule|aviator表达式,不配置默认不走这个逻辑,tenant_code为上面fields定义的字段 ，
如我写的是：tenant_code != nil ，它的意思是tenant_code!=null就同步，具体aviator表达式自行百度|N|
|event|es同步的事件,insert,update,delete,all不配置默认为all|N|
|relations|配置父子文档关联关系，如不需要可不配置|N|
|skips|配置跳过不需要同步的字段，值为dbFields中的字段，支持配置多个|N|
|esFieldTypes|字段类型自定义，字段名称：类型，支持array,json,string,int,date，这里用的dbFields中的字段|N|
|esFields|es字段名称自定义，这里不定义默认同步到es里面的字段名用的是dbFields里面的|N|
|updateArrayFields|更新，添加，删除 es文档中某个数组字段|N|
以上复杂属性详解：
```
relations：
配置父子文档关联关系，如不需要可不配置
配置规则为
relations: 
  customer_join_info: 
        name: customer_info 
        parent: customer_id
customer_join_info    mapping中定义的父子表关联关系
name                         mapping中定义的父或子文档的名称,这里写字符串类型
parent                        如果是子文档这里需要定义父文档的主键id,值是fields中的字段

------------------------------------
updateArrayFields：
更新，添加，删除 es文档中某个数组字段；如果配置该属性则就不会触发fields配置中的字段同步，只会同步updateArrayFields这一项配置，如需要同步es中数组字段则建议单独配置这个同步规则。应用场景
以下es数据中有一个数组字段
{
    "id": 1,
    "name": "张三",
    "hobby_list": ["篮球", "足球"]
}
对应mysql中的数据
Id    hobby
1     篮球
2     足球
3     乒乓球
如果你在插入第三条数据乒乓球
想同步到es里面去让es数据变成
{
    "id": 1,
    "name": "张三",
    "hobby_list": ["篮球", "足球", "乒乓球"]
}
那就要配置这个属性，配置规则为：
hobby_list为es字段名称
hobby为fields中定义的字段名称

updateArrayFields:
    hobby_list: hobby
支持配置多个，如下
updateArrayFields:
    hobby_list1: hobby1
    hobby_list2: hobby2
```

####5、配置rdb冗余同步相关配置
```
shouchebao-canal: 
  ###################################rdb同步相关配置############################################### 
  rdb: #冗余同步方案配置 
    # 配置同步业务1 
    scb_content-channel: #命令规则为：数据库名称-表名称 
      source: 
        #数据库名称 
        database: scb_content 
        #表名称 
        table: channel 
        sourceColumn: 
          #需要冗余同步的id和名称   id:name 
          sid: channel_name 
      targets: #目标资源 
        - customer-info_sync_1: #业务名称 
          #数据源名称 
          datasource: scb-customer 
          #表名 
          table: customer_info 
          targetColumn: #需要同步的表id和名称 
            channel_sid: channel_source_name 
    # 配置同步业务2 命令规则为：数据库名称-表名称 
    scb_tenant-tenant_account_profile: 
      source: 
        sourceColumn: #需要冗余同步的id和名称 
          account_sid: account_name #id:name 
      targets: #目标资源 
        - customer_info: 
          datasource: scb-customer #数据源名称 
          #表名 
          table: customer_info 
          targetColumn: #需要同步的表id和名称 
            tenant_account_id: follow_up_person 
        - test_drive_customer_info: 
          datasource: scb-customer 
          table: test_drive_customer_info #表名 
          targetColumn: #需要同步的表id和名称 
            account_sid: follow_up_person
```
|属性名|描述|是否必填(Y/N)|
|:----    |:---------------------    |:-----|
|scb_content-channel|改属性是由：数据库名称-表名称组成的，一定要按这个规则，可以配置多组|Y|
|sourceColumn|需要同步的源数据库表字段配置|Y|
|targets|目标资源的配置|Y|
|- customer_info|业务名称，无实际作用，可以配置多个|Y|
|datasource|数据源名称，三、里面2、配置数据源信息中配置的数据源|Y|
|table|冗余数据同步目标的表名称|Y|
|targetColumn|冗余数据同步目标的表对应存储的冗余id和冗余名称|Y|
以上复杂属性详解
```
sourceColumn：
需要同步的源数据库表字段配置，配置规则如：
源数据表中的id : 源数据库表中的id对应的要冗余同步的数据
-----------------------------------------------
targetColumn：
冗余数据同步目标的表对应存储的冗余id和冗余名称，如：
你A表冗余存储了B表user_sid和user_name
那这里就配置：
user_sid：user_name
```







