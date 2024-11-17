# Types

## Overview

Each object handled by the system is a directory or a regular file, that's the **base type**. On top of it, a **custom type** can be configured to bring extra meaning to an object.

## Selectors

To configure a *custom type* for a given object, you must describe this object in terms of **selectors** : when all selectors configured for a type match an object, then the type is assigned to the object.

All objects are descibed by the following properties : 

- **name** : the name of the file or directory
- **path** : the absolute path of the file or directory. Its format depends on the underlying file system (Windows/Unix)
- **id** : canonical id built as a relative path in unix style
- **dir?** : *true* when the file is a directory

Let's consider an example where the *root directory* is `c:\My Data\root`.

| source file                              | `name`     | `path`                                 | `id`                   | `dir`? |
| ---------------------------------------- | ---------- | -------------------------------------- | ---------------------- | ------ |
| `c:\My Data\root\readme.txt`             | readme.txt | c:\My Data\root\readme.txt             | readme.txt             | false  |
| `c:\My Data\root\category\blue\file.txt` | file.txt   | c:\My Data\root\category\blue\file.txt | category/blue/file.txt | false  |
| `c:\My Data\root\doc`                    | doc        | c:\My Data\root\doc                    | doc                    | true   |
| `c:\My Data\root\doc\photos\home`        | home       | c:\My Data\root\doc\photos\home        | doc/photos/home        | true   |

By default, all selectors that involve string matching, are applied to the **name** property. This can be changed by configuring the property to be used, via the `property` property.

For example : 

- the type "JPG Image" is assigned to all regular files with a `name` that ends with ".jpg". 

```yaml
types
  - name: "JPG Image"
    selectors:
      - is-directory: false
      - ends-with: ".jpg"
```

- the type "readme file" is assigned to all regular files with a `name` that equals to "readme.txt" or "README.md". 

```yaml
types
  - name: "readme file"
    selectors:
      - is-directory: false
      - equals: 
        - readme.txt
        - README.md
```
- the type "backup file" is assigned to all regulat files with a `id` that starts with "bck", so basically all files under the *bck* folder (eg: "bck/file.txt", "bck/image/img.jpg", etc ...)  

```yaml
types
  - name: "backup file"
    selectors:
      - is-directory: false
      - property: id
      - starts-with: "bck" 
```

### ends-with

Selects the object if its **`name` ends with** a string or with one of the configured strings.

Optional property : `property`

Example:
```yaml
# must end with ".txt"
ends-with: ".txt"

# must end with one of ".jpg" or ".jpeg"
ends-with: 
- ".jpg"
- ".jpeg"
```

### starts-with

Selects the object if its **`name` starts with** a string or with one of the configured strings.

Optional property : `property`

Example:
```yaml
# must start with "file"
ends-with: "file"

# must start with one of "img-" or "img_"
ends-with: 
- "img-"
- "img_"
```

### equals

Selects the object if its **`name` is equals** to a string or to one of the configured strings.

Optional property : `property`

Example:
```yaml
# must be equal to "file.txt"
equals: "file.txt"

# must be equal to one of "image.jpg" or "img.jpg"
equals: 
- "imgage.jpg"
- "img.jpg"
```

### is-directory

Selects the object if it **is a directory**  or if it a **regular file**.

Example:
```yaml
# must be a directory
is-directory: true

# must not be a directory
is-directory: false
```

### matches-regexp

Selects the object if it **matches** a single regular expression or one of the configured regular expressions.

Optional property : `property`

Example:
```yaml
# must match /\d+/
matches-regexp: "\d+"

# must match one of /\w[0-9]+\.txt/ or /[a-zA-Z]+/
matches-regexp: 
- "\w[0-9]+\.txt"
- "[a-zA-Z]+"
```

## ingore

When a type definition includes the `ignore` property with a value set to *true*, then all objects with this type a ignored.

For example, the configuration below defines the type "Temporary dir" to all directories with the name "tmp" or "temp". All directories with this type will be ignored.

```yaml
types
  - name: "Temporary dir"
    selectors:
      - is-directory: true
      - equals: 
        - "tmp"
        - "temp"
    ignore : true
```
