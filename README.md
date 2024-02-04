# FS-Explorer

This project is a basic file system explorer implemented as a fullstack clojure(script) application. Its real purpose is not actually to explore a file system (there are plenty of application that does this very well) but to explore clojure(script) related technologies, library and tools to build fullstack application.

It is derivated from a similar project called [clostack](https://github.com/raoul2000/clostack).

See [User documentation](./doc/README.md).

## Requirements

Note that the project may also run on previous versions, but these are the ones it has been developed on.

- node
```shell
$ node -v
v16.13.1
```
- Java 
```shell
$ java -version
java version "17.0.1" 2021-10-19 LTS
Java(TM) SE Runtime Environment (build 17.0.1+12-LTS-39)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.1+12-LTS-39, mixed mode, sharing)
```

- Clojure
```shell
$ clojure --version
Clojure CLI version 1.10.3.1029
```

Recommended:
- [Visual Studio Code](https://code.visualstudio.com/) + [Calva extension](https://marketplace.visualstudio.com/items?itemName=betterthantomorrow.calva)


## Getting started

- install project
```
$ git clone https://github.com/raoul2000/fs-explorer.git
$ cd fs-explorer
$ npm install
```

## Work on the Frontend

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

## Work on the Backend

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

### Build for Production

- **first** build the frontend app
```shell
$ shadow-cljs release app
or
$ npm run release
```
- **then** build final *jar* into the `./target` folder
```shell
$ clojure -T:build ci
```


### VSCode REST client

This project includes the [REST Client extension](https://marketplace.visualstudio.com/items?itemName=humao.rest-client) for VSCode. It requires minimal configuration to add to the `.vscode/settings.json` configuration file.


```json
"rest-client.environmentVariables": {
	"local": {
		"version": "v2",
		"baseUrl": "http://localhost:8890"
	}
}
```
Update `baseUrl` property to match your settings customization.

REST scripts are stored in `test/bask/http`.