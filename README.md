# spring-kotlin-configuration-properties

## quick start
```
    $ make help
    $ make boot-run.dev  
    $ make boot-run.prod  

```

## the trick: spring binder + jmespath (aka 'jq') ...
```
    data class MyAwesomeConfig(val url:String, user:String, pass:String, timeout:Duration)

    val conf:MyAwesomeConfig = env.jmespath("app.http-client.config")  
    
```

## stuff (untyped rest-api)
```
$ curl http://localhost:8080/api/environment?jq=spring
 -> response: {"data":{"profiles":{"active":"prod"},"beaninfo":{"ignore":"true"},"main":{"banner-mode":"off"},"servlet":{"multipart":{"max-file-size":"50MB","max-request-size":"50MB"}}}}

$ curl http://localhost:8080/api/environment?jq=spring.servlet.multipart
 -> response: {"data":{"max-file-size":"50MB","max-request-size":"50MB"}}

$ curl http://localhost:8080/api/environment?jq=app.service.qualifiedName
```