# spring-kotlin-configuration-properties

How to resolve spring configuration properties - as immutable data class ?

use: spring binder

## quick start
```
    $ make help
    $ make boot-run.dev  
    $ make boot-run.prod  

```

## spring binder
```

val spring:Any? = env.decode("spring") { JSON.convertValue(it) }
val app:Any = env.decode("app") { JSON.convertValue(it) }
val items:List<String> = env.decode("app.example.job.items") { JSON.convertValue(it) }

```


## spring binder + jmespath (aka 'jq') ...
```
    data class MyAwesomeConfig(val url:String, user:String, pass:String, timeout:Duration)

    val conf:MyAwesomeConfig = env.jmespath("app.http-client.config")  
    
```

## stuff (untyped rest-api example)
```
$ curl http://localhost:8080/api/environment?jq=spring
 -> response: {"data":{"profiles":{"active":"prod"},"beaninfo":{"ignore":"true"},"main":{"banner-mode":"off"},"servlet":{"multipart":{"max-file-size":"50MB","max-request-size":"50MB"}}}}

$ curl http://localhost:8080/api/environment?jq=spring.servlet.multipart
 -> response: {"data":{"max-file-size":"50MB","max-request-size":"50MB"}}

$ curl http://localhost:8080/api/environment?jq=app.service.qualifiedName

$ curl http://localhost:8080/api/environment?jq=app.tricky.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q

```