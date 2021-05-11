[![Build Status](https://travis-ci.org/NormanGyllenhaal/canal-client.svg?branch=master)](https://travis-ci.org/NormanGyllenhaal/canal-client)
## 易用的canal 客户端 easy canal client

### 介绍
canal 是阿里巴巴mysql数据库binlog的增量订阅&消费组件  
使用该客户端前请先了解canal,https://github.com/alibaba/canal  
canal 自身提供了简单的客户端，如果要转换为数据库的实体对象，处理消费数据要每次进行对象转换。
该客户端直接将canal的数据原始类型转换为各个数据表的实体对象，并解耦数据的增删改操作，方便给业务使用。

### 要求
java8+

### 特性
* 解耦单表增删操作
* kafka客户端支持
* 同步异步处理支持
* spring boot 开箱即用

### 如何使用
spring boot 方式 
maven 依赖
```xml
<dependency>
    <groupId>com.pateo.qingcloud.canal</groupId>
    <artifactId>canal-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
###开始使用
####1.配置canal需要订阅的kafka队列相关配置
```
canal:
  #kafka连接地址多个用,号分割
  server: kafka1.sit.ptcloud.t.home:9092,kafka2.sit.ptcloud.t.home:9092,kafka3.sit.ptcloud.t.home:9092
  #需要订阅的topic多个用,号分割
  destination: retail_nuat_009_content_channel,retail_nuat_009_tenant_account_profile
  #这里固定写kafka
  mode: kafka
  #是否异步
  async: true
  #推荐同一个group只能订阅一个topic，要不然会出现消费异常多个用,号分割
  group-id: canal-client-content-channel,canal-client-tenant-account-profile
```

####2.配置需要同步到的es的地址
```
#es集群配置
elasticsearch:
  #集群地址多个用,号分割
  esAddress: 172.16.42.15:8200 
  #连接超时时间
  connectTimeOut: 10000
  #客户端从服务器读取数据的超时时间
  socketTimeOut: 9000
  #获取连接的超时时间
  connectionRequestTimeOut: 5000
  #最大连接数
  maxConnectNum: 30
  #最大路由连接数
  maxConnectPerRoute: 10
  #keepAlive保活策略
  keepAliveMinutes: 10
  #用户名
  username: retail
  #密码
  password: xxxxxx
```
####3.配置mysql数据源
```
这里数据源是专门给rdb冗余同步的时候使用，如不需要则可以不配置，每个数据源需要取一个数据源的名称，
方便在下面配置冗余同步的时候使用，如： scb-customer，scb-content都是一个自定义的数据源名称，可以按这种规则配置多个数据源。
```
```
pateo:
  datasource: #数据源配置,此数据源用于给下面rdb冗余同步用
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
        
    #初始化时建立物理连接的个数
    initialSize: 5
    #最小连接池数量
    minIdle: 5
    # 最大连接数
    maxActive: 20
    # 连接等待超时时间
    maxWait: 30000
    # 配置检测可以关闭的空闲连接间隔时间
    timeBetweenEvictionRunsMillis: 3000
    # 配置连接在池中的最小生存时间
    minEvictableIdleTimeMillis: 30001
    #用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。
    testOnBorrow: true
    #建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
    testWhileIdle: true
```
####4.配置es同步的规则
这里是配置es的同步规则，如scb_customer-customer_info可以配置多个，它的命名规则是：
数据库名称-表名，表示我要同步这个库这张表的数据
```
特别支持：支持把某张表数据同时同步到2个es索引里面去，如下配置
- customer-info1这个配置支持数组，如在scb_customer-customer_info配置下有
- customer-info1:、- customer-info-zzz:，就代表把scb_customer库customer_info这张表数据同时同步到：
- customer-info1:、- customer-info-zzz:2个不通配置规则的es索引里面去，详细如下配置文件
```
```
pateo:
###################################es同步相关配置###############################################
  esSync:
    #配置命名规则，数据库名称-表名，可以配置多个
    scb_customer-customer_info: 
      #配置需要同步到es里面的规则，这个可以配置多个
      - customer-info1:
        #需要同步的字段,号分割
        fields: sid,tenant_code,tenant_account_id
        #es索引名称
        index: customer_info_index
        #自定义索引后缀名, tenant_code为上面fields定义的字段
        indexDynamicFieldSuffix: tenant_code 
        id: sid #es主键id
        #es主键id前缀，定义一个字符串
        idPrefix: test_zz
        #aviator表达式,tenant_code为上面fields定义的字段，当满足aviator条件时才会同步，不配置的话代表直接同步
        syncRule: tenant_code != nil
        #同步的事件，insert,update,delete,all,不配置默认为all
        event: insert,update,delete
        # 配置父子文档关联关系
        relations:
          customer_join_info:
            name: customer_info
            parent: customer_id
        #需要跳过不同步的字段
        skips:
          - tenant_code
          - sid
        #字段类型自定义，字段名称：类型，支持array,json,string,int,date,不配置的字段默认string
        objFields: 
          sid: int
          follow_date: date
        #es字段别名设置数据库字段名称：es字段名称
        aliasFields:
          tenant_code: tenant_code1
          #把指定字段插入到es某个数组里面，
          #删除时需要删除数组里面数据,配置这个只会同步updateArrayFields里面定义的字段
        updateArrayFields: 
          - label_value_list: label_value_name #es数组字段：fields中定义的字段
       - customer-info-zzz:
         fields: sid,tenant_code,tenant_account_id,
       ······
         updateArrayFields:
           - label_value_list: label_value_name
```
如上配置详细介绍
配置说明

|属性|描述| |
|:----    |:---------------------    |:-----|
|fields|需要同步的数据库字段，多个用英文,号分割|
|index| es的索引名称(字符串)|
|indexDynamicFieldSuffix| 自定义索引后缀名，值是fields中定义某一个字段|
|id| es文档主键id，值是fields中定义某一个字段|
|idPrefix| 自定义es文档主键id前缀，值是任意字符串|
|syncRule| 值为aviator表达式，如要用请自行百度aviator，满足表达式时才会走同步，否则不同步，不配置的话默认是同步，可以用fields中的字段来做一些判断，如 tenant_code != nil，代表是tenant_code != null就走同步流程|
|event| 同步的事件，在数据库发生了什么事件时才进行同步，nsert,update,delete,all,不配置默认为all|
|relations| 配置父子文档关联关系，如不需要可不配置,配置规则为customer_join_info为mapping中定义的父子表关联关系，name为mapping中定义的父或子文档的名称,这里写字符串类型，parent：如果是子文档这里需要定义父文档的主键id,值是fields中的字段|
|skips| 跳过fields中某些字段不同步到es中去，场景：如这个属性indexDynamicFieldSuffix中需用到fields中的字段，但是你又不想这个字段同步到es里面去，就需要在这里配置一下这个属性，数组格式可以配置多个。|
|objFields| 字段类型自定义可以配置多个,用fields中的字段名称进行配置，支持array,json,string,int,date,不配置的字段默认string，详细解释如下：|
```
objFields:  
    sid: int
    follow_date: date
```
|属性|描述|默认值|
|:----    |:---------------------    |:-----|
|aliasFields| es字段别名设置可以配置多个      fields中的字段名称：es自定义字段名称 如下这样配置：|
```
aliasFields: 
  tenant_code: tenant_code1
  sid: sidzz
```
|属性|描述|默认值|
|:----    |:---------------------    |:-----|
|updateArrayFields|更新，添加，删除 es文档中某个数组字段；如果配置该属性则就不会触发fields配置中的字段同步，只会同步updateArrayFields这一项配置，如需要同步es中数组字段则建议单独配置这个同步规则。应用场景
```
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


####5.配置rdb数据库冗余字段同步
如下配置是配置数据库冗余字段同步，如无需求则可不配置
冗余同步配置可以配置多个，如下面配置文件中，scb_content-channel它是有命名规则的，命令规则为：数据库名称-表名称，
比如下面的配置中scb_content-channel、scb_content-user:是代表配置了2组数据库冗余同步规则方案
```
source下属性配置详解
    database           需要同步的源数据库名称
    table                  需要同步的源数据库下的表名称
    sourceColumn   需要冗余同步的id和名称   配置规则如：id:name

targets下属性配置详解
- customer-info_sync_1  这个名字自定义无实际作用，用于区分同步业务，数组格式可以配置多个多 个 - xxx配置代表把上面的source的数据同时同步到多个目标 数据库中。
如下面配置中：- customer-info_sync_1、- customer-info_sync_2代表了把上面的source中数据同时同步到2组冗余数据同步中。简单点理解就是A库中的A表name字段的数据发生了变化时，同时B，C二个库有冗余存储这个A表中name冗余数据，这时需要把A表中name字段同步到：B库B表 中的name字段上和C表C库的name字段上。

datasource      配置3.配置mysql数据源 配置中的数据源名称
table               同步到目标库的表名称
targetColumn  目标表字段名称: 目标表字段名称

```
具体请看下面的配置:
```
pateo:
  ###################################rdb同步相关配置###############################################
  rdb: #冗余同步方案配置
    # 配置同步业务1
    #命令规则为：数据库名称-表名称,可以配置多个，类似scb_content-channel这样的可以配置多个
    scb_content-channel:
      source:
        #需要同步的源数据库名称
        database: scb_content
        #需要同步的源数据库下的表名称
        table: channel
        sourceColumn:
          #需要冗余同步的id和名称   id:name
          sid: channel_name
      targets: #目标资源
       #业务名称，无实际作用
        - customer-info_sync_1:
          #数据源名称
          datasource: scb-customer
          #表名
          table: customer_info
          #需要同步的表id和名称
          targetColumn:
            #目标表字段名称: 目标表字段名称
            channel_source: channel_source_name
         - customer-info_sync_2:
           ·········此处省略相同的配置
    scb_content-user:
      source:
        #数据库名称
        database: scb_content
        ··············此处省略相同的配置
```
以上配置属性介绍表：

|属性|描述|默认值|
|:----    |:---------------------    |:-----|
|database|源数据库名称|
|sourceColumn| 源数据需要冗余同步的id和名称 只能配置一对，如下：|

```
sourceColumn: 
    account_sid: account_name

account_sid            源数据库表的id字段
account_name          源数据库表的name字段
```
|属性|描述|默认值|
|:----    |:---------------------    |:-----|
|datasource|源数据库名称|
|table| 同步的目标表名称|
|targetColumn| 目标数据同步的id和值可以配置多组，列如：|
```
targetColumn: 
   channel_source: channel_source_name


channel_source              需要同步的目标数据库id字段名称
channel_source_name         需要同步的目标数据库name字段名称
```



###完整的配置说明如下
```

canal: 
  #kafka连接地址多个用,号分割 
  server: kafka1.sit.ptcloud.t.home:9092,kafka2.sit.ptcloud.t.home:9092,kafka3.sit.ptcloud.t.home:9092 
  #需要订阅的topic多个用,号分割 
  destination: retail_nuat_009_content_channel,retail_nuat_009_tenant_account_profile 
  #这里固定写kafka 
  mode: kafka 
  #是否异步 
  async: true 
  #推荐同一个group只能订阅一个topic，要不然会出现消费异常多个用,号分割 
  group-id: canal-client-content-channel,canal-client-tenant-account-profile 
#es集群配置 
elasticsearch: 
  #集群地址多个用,号分割 
  esAddress: 172.16.42.15:8200  
  #连接超时时间 
  connectTimeOut: 10000 
  #客户端从服务器读取数据的超时时间 
  socketTimeOut: 9000 
  #获取连接的超时时间 
  connectionRequestTimeOut: 5000 
  #最大连接数 
  maxConnectNum: 30 
  #最大路由连接数 
  maxConnectPerRoute: 10 
  #keepAlive保活策略 
  keepAliveMinutes: 10 
  #用户名 
  username: retail 
  #密码 
  password: xxxxxx 
pateo: 
  datasource: #数据源配置,此数据源用于给下面rdb冗余同步用 
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
         
    #初始化时建立物理连接的个数 
    initialSize: 5 
    #最小连接池数量 
    minIdle: 5 
    # 最大连接数 
    maxActive: 20 
    # 连接等待超时时间 
    maxWait: 30000 
    # 配置检测可以关闭的空闲连接间隔时间 
    timeBetweenEvictionRunsMillis: 3000 
    # 配置连接在池中的最小生存时间 
    minEvictableIdleTimeMillis: 30001 
    #用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。 
    testOnBorrow: true 
    #建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。 
    testWhileIdle: true 
         
  esSync: 
    #配置命名规则，数据库名称-表名，可以配置多个 
    scb_customer-customer_info:  
      #配置需要同步到es里面的规则，这个可以配置多个 
      - customer-info1: 
        #需要同步的字段,号分割 
        fields: sid,tenant_code,tenant_account_id,org_code,wx_open_id,channel_source,channel_source_name,follow_date,following_flag,channel_type,channel_value,version,create_by,create_date,update_by,update_date,customer_flag,follow_up_person,last_follow_time,next_follow_time,read_flag,last_result_sid,last_result_name,clue_level_sid,clue_level_name,phone,delete_flag,task_flag,follow_count,tenant_name,parent_tenant_code,tenant_flag 
        #es索引名称 
        index: customer_info_index 
        #自定义索引后缀名, tenant_code为上面fields定义的字段 
        indexDynamicFieldSuffix: tenant_code  
        id: sid #es主键id 
        #es主键id前缀，定义一个字符串 
        idPrefix: test_zz 
        #aviator表达式,tenant_code为上面fields定义的字段，当满足aviator条件时才会同步，不配置的话代表直接同步 
        syncRule: tenant_code != nil 
        #同步的事件，insert,update,delete,all,不配置默认为all 
        event: insert,update,delete 
        # 配置父子文档关联关系 
        relations: 
          customer_join_info: 
            name: customer_info 
            parent: customer_id 
        #需要跳过不同步的字段 
        skips: 
          - tenant_code 
        #字段类型自定义，字段名称：类型，支持array,json,string,int,date,不配置的字段默认string 
        objFields:  
          sid: int 
          follow_date: date 
        #字段别名  数据库字段名称：es字段名称 
        aliasFields: 
          tenant_code: tenant_code1 
          #把指定字段插入到es某个数组里面， 
          #删除时需要删除数组里面数据,配置这个只会同步updateArrayFields里面定义的字段 
        updateArrayFields:  
          - label_value_list: label_value_name #es数组字段：数据库字段 
       - customer-info-zzz: 
         fields: sid,tenant_code,tenant_account_id, 
       ······ 
         updateArrayFields: 
           - label_value_list: label_value_name 
  rdb: #冗余同步方案配置 
    # 配置同步业务1 
    #命令规则为：数据库名称-表名称,可以配置多个，类似scb_content-channel这样的可以配置多个 
    scb_content-channel: 
      source: 
        #需要同步的源数据库名称 
        database: scb_content 
        #需要同步的源数据库下的表名称 
        table: channel 
        sourceColumn: 
          #需要冗余同步的id和名称   id:name 
          sid: channel_name 
      targets: #目标资源 
       #业务名称，无实际作用 
        - customer-info_sync_1: 
          #数据源名称 
          datasource: scb-customer 
          #表名 
          table: customer_info 
          #需要同步的表id和名称 
          targetColumn: 
            #目标表字段名称: 目标表字段名称 
            channel_source: channel_source_name 
         - customer-info_sync_2: 
           ·········此处省略相同的配置 
    scb_content-user: 
      source: 
        #数据库名称 
        database: scb_content 
        ··············此处省略相同的配置
```
