## About [![Build Status](https://travis-ci.org/dernasherbrezon/fcdwClient.svg?branch=master)](https://travis-ci.org/dernasherbrezon/fcdwClient) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ru.r2cloud%3AfcdwClient&metric=alert_status)](https://sonarcloud.io/dashboard?id=ru.r2cloud%3AfcdwClient)

Client for [FunCube Data Warehouse](http://data.amsat-uk.org/)

## Usage

* [Register](http://data.amsat-uk.org/registration) at the warehouse.
* Confirm your email and make notice of "Site Id" and "Auth Code" from the email
* Configure pom.xml:

```xml
<dependency>
    <groupId>ru.r2cloud</groupId>
    <artifactId>fcdwClient</artifactId>
    <version>1.0</version>
</dependency>
```
* Configure client:

```java
FcdwClient client = new FcdwClient("http://data.amsat-uk.org", SITE_ID, AUTH_CODE);
client.upload("9d6470cd32f4971a56a4d7c7714b40d3");
```