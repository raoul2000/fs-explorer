# Types

## Selector

### ends-with

Selects the file item if it **ends with** a string or with one of the configured strings.

Example:
```yaml
# must end with ".txt"
ends-with: ".txt"

# must end with one of ".jpg" ".jpeg"
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

# must start with one of "img-" "img_"
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

# must be equal to one of "image.jpg" "img.jpg"
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