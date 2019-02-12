# spring-kotlin-configuration-properties

How to resolve spring configuration properties - as immutable data class ?

use: 
- spring-binder (see: https://spring.io/blog/2018/03/28/property-binding-in-spring-boot-2-0)
- spring-binder + jmespath (see: http://jmespath.org/)

## quick start
```
    $ make help
    $ make boot-run.dev  
    $ make boot-run.prod  

```

## approaches

- spring-binder
- spring-binder + jmespath

Note: None of them will raise exceptions on missing environment variables.


## spring-binder
```

val spring:Any? = env.decode("spring") { JSON.convertValue(it) }
val app:Any = env.decode("app") { JSON.convertValue(it) }
val items:List<String> = env.decode("app.example.job.items") { JSON.convertValue(it) }

```


## spring-binder + jmespath (aka 'jq') ...
```
    data class MyAwesomeConfig(val url:String, user:String, pass:String, timeout:Duration)

    val conf:MyAwesomeConfig = env.jmespath("app.http.client.config")  
    
```

## rest-api example: jmespath

Findings:
- Direct access to List-types: returns Map type
- Direct access to List-Types might be solved, e.g: 
```
    val items:List<String> = env.jq("app.example.job.items") { JSON.convertValue<Map<Any,String>>(it).values.toList() }
```                                                          
- Does not allow direct access to kebab-case properties, e.g: app.service.q-name
- Allows access to object that contains kebab-case properties, e.g. app.service

```
$ curl http://localhost:8080/api/environment/jmespath/v1?q=spring
 -> response: {"data":{"profiles":{"active":"prod"},"beaninfo":{"ignore":"true"},"main":{"banner-mode":"off"},"servlet":{"multipart":{"max-file-size":"50MB","max-request-size":"50MB"}}}}

$ curl http://localhost:8080/api/environment/jmespath/v1?q=spring.servlet.multipart
 -> response: {"data":{"max-file-size":"50MB","max-request-size":"50MB"}}

$ curl http://localhost:8080/api/environment/jmespath/v1?q=app.service.qualifiedName
 -> response: {"data":"example-service-prod"}
 
$ curl http://localhost:8080/api/environment/jmespath/v1?q=app.service.q-name
 ->  Exception: Unable to compile expression \"app.service.q-name\": syntax error
 
$ curl http://localhost:8080/api/environment/jmespath/v1?q=app.serviceXXXXX.qualifiedName
 -> response: {"data":null}

$ curl http://localhost:8080/api/environment/jmespath/v1?q=app.tricky.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p
 -> response: {"data":{"q":{"0":"d0","1":"d1","2":"d3-example-service-prod"}}}
 
$ curl http://localhost:8080/api/environment/jmespath/v1?q=app.tricky.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q
 -> response: {"data":{"0":"d0","1":"d1","2":"d3-example-service-prod"}}

```

## rest-api example: spring-binder 

Findings:

- Fails on direct access of camelCase properties, e.g. app.service.qualifiedName
- Direct access to kebab-case properties works: app.service.q-name
- Access to objects containing camelCase properties works: e.g.: app.service
- Direct access to List-types: returns List
- Access to object containing List types: returns object containing Map type


```
$ curl http://localhost:8080/api/environment/bind/v1?q=app.service
 -> response: {"data":{"name":"example-service","qualifiedName":"example-service-prod","q-name":"example-service-prod"}}

$ curl http://localhost:8080/api/environment/bind/v1?q=app.service.q-name
 -> Exception: Configuration property name 'app.service.qualifiedName'

$ curl http://localhost:8080/api/environment/bind/v1?q=app.service.q-name
 -> response: {"data":"example-service-prod"}

$ curl http://localhost:8080/api/environment/bind/v1?q=app.service.foo
 -> response: {"data":null}
 
$ curl http://localhost:8080/api/environment/bind/v1?q=app.foo.bar
 -> response: {"data":null} 

$ curl http://localhost:8080/api/environment/bind/v1?q=spring.servlet.multipart
 -> response: {"data":{"max-file-size":"50MB","max-request-size":"50MB"}}

$ curl http://localhost:8080/api/environment/bind/v1?q=app.tricky.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p
 -> response: {"data":{"q":{"0":"d0","1":"d1","2":"d3-example-service-prod"}}}

$ curl http://localhost:8080/api/environment/bind/v1?q=app.tricky.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q
 -> response: {"data":["d0","d1","d3-example-service-prod"]}

```

## spring-binder: kebab-case - whats wrong?

A real world scenario:

"I as a developer, want to start java app and override the hikari-pool-configuration"

solution:  (see: https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby )

    -> spring.datasource.hikari.maximumPoolSize=50

```
# docker-compose.yml

command: ["java"]
args: [
  "-jar",
  "-Dspring.profiles.active=dev,flyway-migrate",
  "-Dlogging.level.com.zaxxer.hikari=debug",
  "-Dspring.datasource.hikari.maximumPoolSize=50",
  "-Xms32m",
  "-Xmx1024m",
  "/opt/app/app.jar --debug"
]
```


```
# log the maximumPoolSize
val maximumPoolSize:Any? = env.decode("spring.datasource.hikari.maximumPoolSize") { it }
    --> this will fail, since "maximumPoolSize" is not kebab-case ;)

logger.info("maximumPoolSize: $maximumPoolSize")
```

The issue:

(see: https://spring.io/blog/2018/03/28/property-binding-in-spring-boot-2-0)

- spring stores the "properties" into a "java.util.Set".
- access to Set should be "relaxed". (e.g.: person.first-name, person.firstName and PERSON_FIRSTNAME)
- I guess when adding an entry to the set, the key will be "kebab-cased + lower-cased".

A drawback of that approach ...

- developer needs to know the internals
- it feels rather "magic" than straight-forward.

```
$ java -jar -Dspring.fooBar -> env.get("spring.foo-bar")
```



