# JSF Chat App (GlassFish 5.1, Java 8, PostgreSQL)

## Prérequis
- OpenJDK 1.8
- GlassFish 5.1.0
- PostgreSQL 14+
- Maven 3.8+

## Installation DB
Créez la base et l'utilisateur:
```sql
CREATE DATABASE chatdb;
CREATE USER chatuser WITH ENCRYPTED PASSWORD 'chatpass';
GRANT ALL PRIVILEGES ON DATABASE chatdb TO chatuser;
```

Créez les tables:
```sql
\i sql/schema_postgres.sql
```

Ajoutez un utilisateur admin:
```sql
INSERT INTO users(username, passwordhash, fullname) VALUES
('admin', '$2a$10$EJr6n2h0hU7cNq8qj9O7D.2yYFQeV2uHqg8f7zv7pM0a2m8M4o8uK', 'Administrateur');
-- password: admin123
```

## GlassFish: datasource
Déployez la ressource JDBC (méthode 1 - via asadmin):
```
asadmin start-domain
asadmin create-jdbc-connection-pool --restype javax.sql.DataSource --datasourceclassname org.postgresql.ds.PGSimpleDataSource --property user=chatuser:password=chatpass:databaseName=chatdb:serverName=localhost:portNumber=5432 PostgresPool
asadmin create-jdbc-resource --connectionpoolid PostgresPool jdbc/appDS
```

Ou déployez `WEB-INF/glassfish-resources.xml` depuis l'admin console (Resources > JDBC > JDBC Resources).

## Build & Déploiement
```
mvn clean package
asadmin deploy target/jsf-chat-app.war
```

## Utilisation
- Ouvrez `http://localhost:8080/jsf-chat-app/login.xhtml`
- Connectez-vous avec `admin / admin123`
- Accédez au tchat: `/app/tchat.xhtml`
- Les messages s'affichent initialement depuis JPA et ensuite en temps réel via WebSocket `/ws/tchat`.

## Notes
- JPA utilise JTA et la datasource `jdbc/appDS`.
- WebSocket endpoint injecte le service via `CDI.current()`.
- AuthFilter protège `/app/*`.
