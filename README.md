# FS-Explorer

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