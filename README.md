# FS-Explorer

## Front

Powered by [shadow-cljs](https://shadow-cljs.github.io/docs/UsersGuide.html).

> Since August 2022, *shadow-cljs* required Java 11 or greater (see [shadow-cljs stop working with java 1.8](https://github.com/thheller/shadow-cljs/issues/1050)). If you still want to stick to your old Java 8, install Java 11 but don't change your `$PATH` environment variable. Then do : 
>```
> $ export PATH="/c/Program Files/Java/jdk-17.0.1/bin:$PATH"
> $ npx shadow-cljs watch app
>```
> This way, another Java version (gere 17) will be used only for *shadow-cljs* and everyone is happy.
> 
> Same applies to all *shadow-cljs* commands.
>

- start *shadow-cljs* server and *watch* changes on the main application
```shell
$ npx shadow-cljs watch app
or
$ npm run watch-app
```

## Back

- run the project directly
```bash
$ clojure -M:run-m
# with options ...
$ clojure -M:run-m --help
```
- run tests
```shell
$ clojure -T:build test
```
- build final *jar* into the `./target` folder
```shell
$ clojure -T:build ci
```
- Run uberjar:
```shell
$ java -jar target/fs-explorer-X.X.X.jar 
```

## Documentation

see [documentation](./doc/README.md)