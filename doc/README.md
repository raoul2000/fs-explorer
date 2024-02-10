# Documentation

## Configuration

When the default application settings must be changed, they can be defined in a JSON file whose path must be added as command line argument : 

```bash
$ java -jar xxx.jar <configuration file path>
```

Following settings can be configured : 

- **server-port** - the server port number. Default is `8890`
- **open-browser** : When TRUE (default) the default browser is opened when the application starts
- **browse-url** : the URL opened by the browser (when **open-brower** is TRUE) on startup. Default is `http://localhost:[PORT]/` where `[PORT]` is the configured port number. 


Example:
```json
{
    "server-port": 8001,
    "open-browser": true
}
```

