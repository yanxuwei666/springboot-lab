# 单元测试练习

独立的 Spring Boot 项目，用于练习单元测试。

- Java 8
- Spring Boot 2.7.18
- `spring-boot-starter-test`：包含 JUnit Jupiter、Mockito 和 Spring 测试支持。

使用 JDK 8 和 Maven，在本目录运行：

```bash
mvn spring-boot:run
```

启动后默认监听 8080 端口。按 `Ctrl+C` 停止服务。

## 测试和覆盖率

运行测试并生成 JaCoCo 覆盖率报告：

```bash
mvn clean test
open target/site/jacoco/index.html
```

在报告中点击包和类，可以查看代码行及分支的执行情况。绿色表示已覆盖，红色表示未覆盖，黄色表示部分覆盖。覆盖率只能反映代码是否被执行，不能证明断言正确或业务场景完整。

报告和检查只统计 `service`、`controller` 包中的类。执行 `mvn clean verify` 时，JaCoCo 检查这两个包整体的行覆盖率；低于 40% 时构建失败。`mvn clean test` 会生成报告，但不会执行覆盖率门槛检查。

## 订单报价练习

`POST /api/v1/orders/quote` 根据单价和数量计算报价，不保存订单。单价须大于 0 且最多两位小数，数量为 1～100 的整数。商品总价满 500 元打九折，否则满 200 元打九五折；折扣金额四舍五入到分。商品总价满 100 元免运费，否则运费 10 元。折扣和运费门槛均按折扣前的商品总价判断。

```bash
curl -X POST http://localhost:8080/api/v1/orders/quote \
  -H 'Content-Type: application/json' \
  -d '{"unitPrice": 100, "quantity": 2}'
```

响应的 `code`、`message`、`data` 参考考勤系统的统一格式。参数不符合规则或请求体格式错误时，订单案例返回 HTTP 400 和错误信息；异常处理器仅作用于订单接口。可以练习测试金额边界、四舍五入、异常、响应及参数传递。
