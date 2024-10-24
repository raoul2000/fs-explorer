# Documentation

## Configuration

When the default application settings must be changed, they can be defined in a YAML file whose path must be added as command line argument : 

```bash
$ java -jar xxx.jar <configuration file path>
```

Following settings can be configured : 

- **server-port** - the server port number. Default is `8890`
- **open-browser** : When TRUE (default) the default browser is opened when the application starts
- **browse-url** : the URL opened by the browser (when **open-brower** is TRUE) on startup. Default is `http://localhost:[PORT]/` where `[PORT]` is the configured port number. 
- **root-dir-path** : absolute path to an existing folder that is used to resolved all dir path to explore. In other words, only files and folders under the `root-dir-path` can be explored by the application. If not set, the application will use the `$HOME` folder (on windows : `C:\Users\%username%`).

Example:
```json
{
    "server-port": 8001,
    "open-browser": true,
    "root-dir-path": "c:\\"
}
```


## Actions

The application provide following defaults actions : 
- user clicks on a *folder* : navigates into this folder.
- user clicks on a *file* : opens a new tab in the browser to display file's content or download the file if it can't be displayed.


It is possible to change default behaviors on *files* by configuring **actions**. In the example below, when the user clicks on a file named `readme.md` or `readme.txt` the build-in command **open** will be executed on this file.

Example : 
```json
{
  "actions": [
      {
        "selector": "readme.md",
        "command": "open"
      },
      {
        "selector": "readme.txt",
        "command": "open"
      }
    ]    
}
```

## Custom Actions

Custom action are first declared by the `actions` key, and then associated with the type of object they should be applied to. For example, in the snippet below we first declare the action "Notepad" and then it is associated with all objects with type "txt file".

```yaml
actions:
  - name: Notepad
    exec: notepad.exe
types:
  - name: txt file
    selectors:
      - ends-with: .txt
      - actions:
        - name: Notepad
```

