# Metadata


## Overview

Each item, wether it is a directory or a regular file, can have extra information to describing it. Metadata informations are stored as a regular file, in JSON or YAML format, and they follow a strict naming convention.

> Although it is possible to mix JSON and YAML formats for metadata files, it is recommended to keep things simple, pick up a format and stick to it.

The data stored as metadata are entirely defined by you, and as long as the metadata document's format is valid and matches the file extension, the system will be able to handle it.


## Naming convention

The metadata file associated with an item must follow the naming convention described below : 

```
[related item's filename].["json" | "yaml" | "yml"].meta
```
Where :
- *related item's filename* 
  - if the item is a *regular file*, this field is the filename **including its extension** 
  - if the item is a *directory* then this field remains blank.
- *["json" | "yaml" | "yml"]*  defines the format of the metadata file

For example : 

| item's filename     | base type    | metadata's filename   | format |
| ----------------    | ------------ |  -------------------- | -------|
| **image.jpg**       | regular file | image.jpg.json.meta   | json   |
| **My Document.doc** | regular file | My Document.yaml.meta | yaml   |
| **documents**       | directory    | .yaml.meta            | yaml   |
| **backup**          | directory    | .json.meta            | json   |

## Location

Two possible cases:

- When the item is a regular file, its associated metadata file must be located in the same directory.
- When the item is a directory, the metadata file must be located in this directory.


