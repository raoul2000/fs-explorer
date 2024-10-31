# Types

## Overview

Each item is a directory or a regular file, that's the **base type**. On top of it, a **custom type** can be configured so to bring extra meaning to items.


## Selectors

To configure a *custom type* for a given item, you must describe this item in terms of **selectors** : when all selectors configured for a type match an item, then the type is assigned to the item.

Let's see that on a simplified example : the configuration below defines 2 custom types, each one using a single selector that matches based on the last characters of the item *name*.

```yaml
types
  - name: "JPG Image"
    selectors:
      - ends-with: ".jpg"
  - name: "PNG Image"
    selectors:
      - ends-with: ".png"
```

Let's consider an example where the *root directory* is `c:\My Data\root`.

| source file                              | name       | path                                   | id                     | dir?  |
| ---------------------------------------- | ---------- | -------------------------------------- | ---------------------- | ----- |
| `c:\My Data\root\readme.txt`             | readme.txt | c:\My Data\root\readme.txt             | readme.txt             | false |
| `c:\My Data\root\category\blue\file.txt` | file.txt   | c:\My Data\root\category\blue\file.txt | category/blue/file.txt | false |
| `c:\My Data\root\doc`                    | doc        | c:\My Data\root\doc                    | doc                    | true  |
| `c:\My Data\root\doc\photos\home`        | home       | c:\My Data\root\doc\photos\home        | doc/photos/home        | true  |


- **name** : the name of the file or directory
- **path** : the absolute path of the file or directory. Its format depends on the underlying file system
- **id** : canonical id built as a relative path in unix style
- **dir?** : *true* when the file is a directory

### ends-with

Selects the file item if it **ends with** a string or with one of the configured strings.

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

Selects the file item if it **starts with** a string or with one of the configured strings.

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

Selects the file item if it **equals** to a string or to one of the configured strings.

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

Selects the file item if it **is a directory or not**.

Example:
```yaml
# must be a directory
is-directory: true

# must not be a directory
is-directory: false
```


### matches-regexp

Selects the file item if it **matches** a single regular expression or one of the configured regular expressions.

Example:
```yaml
# must match /\d+/
matches-regexp: "\d+"

# must match one of /\w[0-9]+\.txt/ or /[a-zA-Z]+/
matches-regexp: 
- "\w[0-9]+\.txt"
- "[a-zA-Z]+"
```