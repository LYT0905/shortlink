# 短链接项目

## 一.什么是短链接

> 就是把普通网址，转换成比较短的网址。比如：[http://t.cn/RlB2PdD](https://link.zhihu.com/?target=http%3A//t.cn/RlB2PdD) 这种，在微博这些限制字数的应用里。好处不言而喻。短、字符少、美观、便于发布、传播。

## 二.技术架构

在系统设计中，采用最新 JDK17 + SpringBoot3&SpringCloud 微服务架构，构建高并发、大数据量下仍然能提供高效可靠的短链接生成服务。

![img](https://images-machen.oss-cn-beijing.aliyuncs.com/image-20231026132606180.png)

## 三.短链接作用

主要作用包括但不限于以下几个方面：

1. **提升用户体验**：用户更容易记忆和分享短链接，增强了用户的体验。
2. **节省空间**：短链接相对于长 URL 更短，可以节省字符空间，特别是在一些限制字符数的场合，如微博、短信等。
3. **美化**：短链接通常更美观、简洁，不会包含一大串字符。
4. **统计和分析**：可以追踪短链接的访问情况，了解用户的行为和喜好。

## 四.项目亮点

1. **海量并发**：可能会面对大量用户同时访问的情况，尤其在高峰期，这会对系统的性能和响应速度提出很高的要求。
2. **海量存储**：可能需要存储大量的用户数据，包括数据库、缓存等，需要足够的存储空间和高效的存储管理方案。
3. **多租户场景**：通常支持多个租户共享同一套系统，需要保证租户间的数据隔离、安全性和性能。
4. **数据安全性**：需要保证用户数据的安全性和隐私，防止未经授权的访问和数据泄露。
5. **扩展性&可伸缩性**：需要具备良好的扩展性，以应对用户数量和业务规模的增长。

## 五.项目原理

![img](https://images-machen.oss-cn-beijing.aliyuncs.com/image-20231115133642504.png)
