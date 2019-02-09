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