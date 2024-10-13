# Actions

Actions can be configured to be associated with types (see [Types](./types.ms)).

There is two kinds of actions :

- Built-in actions
- Custom actions

Actions can be fully configured at type level, or globally and then referenced by the type they should be associated with (see [Custom Actions](#custom-actions)).

## Built-in Actions

Built-in actions require minimal configuration as they don't need any extra arguments or options to be used.

### *open*

This action invoked the default *open* shell command if it is provided by the underlying operating system. In Windows for example, this action will invoke the application registered for the given file type (Notepad.exe will be launched on *txt* files). 

It takes no additional argument.

Example : 

```yaml
- actions:
    - name: Open File
      exec: open
```

### *download*

This action causes the selected file to be downloaded by the browser.

It takes no additional argument.

Example : 

```yaml
- actions:
    - name: Download File
      exec: download
```

### *view*

This action tries to display the selected file in a new browser's tab. If the content (read MIME Type) of the file is not supported for browser preview, then the file is downloaded.

It takes no additional argument.

Example : 

```yaml
- actions:
    - name: View File
      exec: view
```

## Custom Actions

You can use the configuration file to declare new actions. Following options are available :

- `name` : required - the unique name of the action.
- `exec` : required - the program that should be launched to run the action.
- `args` : (optional) the argument or the list of arguments that will be passed to the program with the command line.
- `wait` : (optional, boolean, default is *FALSE*) launch the program asynchronously or not.


