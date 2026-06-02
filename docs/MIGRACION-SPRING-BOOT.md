# Migración Spring Boot 4

## Estado: completada (base)

- Spring Boot **4.0.6**, Java 17
- Starters: `webmvc`, `jdbc`, `jackson` (Jackson 3)
- Quartz manual (`QuartzSchedulerConfig`) — un solo scheduler
- Pool JDBC Tomcat en `application.yml`
- `spring.threads.virtual.enabled=false`

## Configuración

| Qué | Dónde |
|-----|--------|
| BD / pool | `application.yml` → `datasource.jndi` |
| Puerto | `application.yml` → `server` |
| Quartz / ambiente | `src/main/env/env.dev` \| `env.pro` → `ibMsgEnv.properties` (`-Denv=`) |
| Conexion mail/SMS | `MENT_CHANNEL` (BD). `app_env` solo reglas de envio |
| Logs | `log4j.xml` → `logs/ibMsgLogging.log` |

## Jackson 3

- API: `tools.jackson.databind` (`JsonMapper`, `TypeReference`)
- Anotaciones: `com.fasterxml.jackson.annotation` (`@JsonIgnore`, `@JsonFormat`)
- REST `/getMsg`: body `GetMsgRequest` con `id_msg`

## Compilar / ejecutar

```bash
./build.sh                    # dev (DE)
mvn clean package -Denv=pro   # produccion (PR)
mvn spring-boot:run
```

IntelliJ: en Maven/Run usar `-Denv=dev` o `-Denv=pro` (debe ejecutarse `process-resources` antes de arrancar).
