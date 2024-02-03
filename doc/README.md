# Documentation

## Configuration

When the default application settings must be changed, they can be defined in a JSON file whose path must be added as command line argument : 

```bash
$ java -jar xxx.jar <configuration file path>
```

Following settings can be configured : 

- **server-port** - default 8890 

Example:
```json
{
    "server-port": 8001
}
```

