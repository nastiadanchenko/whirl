[![Build Status](https://scrutinizer-ci.com/g/whirlplatform/whirl/badges/build.png?b=master)](https://scrutinizer-ci.com/g/whirlplatform/whirl/build-status/master)

<p style="text-align:center;">
  <img src="logo.png" />
</p>

# Whrl Platform

The Whirl Platform is application builder software for database developers that simplifying of creating web
applications. It provides WYSIWYG tools for building UI that tightly binds to database data and business logic. Building
application didn't require to write application server or client side logic, all logic can be done database side.

Platform is on production-ready state and used in more than twenty closed source commercial applications.

## Demo

You can try Whirl Platform on the our demo server.

#### Application

Server: [Showcase application](http://whirl-demo.jelastic.regruhosting.ru/app?application=whirl-showcase)

Username: whirl-showcase-user

Password: password

#### Editor

Server: [Editor](http://whirl-demo.jelastic.regruhosting.ru/editor/)

Username: whirl-admin

Password: password

## License

Since the Whirl Platform client side code mostly based on the Sencha GXT library, it's deriving GPL v3 license.

[License text](LICENSE)

## Developing

First the database to store platform data should be created.

- **PostgreSQL**

  For PostgreSQL you should have configured local RDBMS on 5432 port.
  SQL scripts for creating metadata database are:

    ```sql
    CREATE ROLE whirl WITH LOGIN PASSWORD 'password';
    CREATE DATABASE whirl OWNER whirl;
    GRANT ALL PRIVILEGES ON DATABASE whirl TO whirl;
    -- connect to whirl database as superuser and run next commands
    CREATE SCHEMA whirl AUTHORIZATION whirl;
    ```

- MySQL:
  For MySQL configure it on port 3306.
    ```sql
    CREATE USER whirl IDENTIFIED BY 'password';
    CREATE DATABASE whirl;
    GRANT ALL ON whirl.* TO whirl;
    ```

To prepare dependencies for running platform in development mode you should build prerequisites:

```bash
mvn clean install -Dgwt.skipCompilation=true
```

### Main platform - whirl-app


#### Server

Command to start backend on Tomcat server is:

```bash
cd whirl-app/whirl-app-server
mvn compile war:exploded cargo:run -pl whirl-app-server -am -P jdbc-postgresql,config-postgresql,local-store
```

#### Client

We are using GWT for developing frontend side
with [tbroyer Maven GWT plugin](https://tbroyer.github.io/gwt-maven-plugin/index.html) to manage GWT modules.

Command to start frontend in dev mode is:
```bash
cd whirl-app/whirl-app-server
mvn gwt:codeserver -pl whirl-app-client -am
```

After command execution application will be accessible at http://localhost:8090/app. Frontend part will be compiled on
demand.

### Application editor - whirl-editor

#### Server

Commands to start:
```bash
cd whirl-editor/whirl-editor-server
mvn compile war:exploded cargo:run -pl whirl-editor-server -am -P jdbc-postgresql,config-postgresql,local-store
```
#### Client

Commands to start:
```bash
cd whirl-editor
mvn gwt:codeserver -pl whirl-editor-client -am
```

Editor will be accessible at http://localhost:8091/editor/.

### Troubleshooting

If you experience the following issue while initializing maven project:

    Blocked mirror for repositories: [jboss-central (http://repository.jboss.org/nexus/content/groups/public-jboss, default, releases...

Add following to your maven settings.xml:

```xml
<mirrors>
  <mirror>
    <id>imagej-repository-mirror</id>
    <name>ImageJ repository mirror</name>
    <url>https://maven.imagej.net/content/repositories/public</url>
    <mirrorOf>imagej-repository</mirrorOf>
  </mirror>
  <mirror>
    <id>jboss-repository-mirror</id>
    <name>JBoss Public Nexus Repository</name>
    <url>https://repository.jboss.org/nexus/content/groups/public/</url>
    <mirrorOf>jboss-central</mirrorOf>
  </mirror>
</mirrors>
```

### Release preparation

Command to set new version in all pom.xml files is:
```bash
mvn versions:set -DnewVersion=X.X.X-SNAPSHOT -DprocessAllModules
```