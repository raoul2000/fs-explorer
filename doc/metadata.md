# Metadata


## Overview

Each item, wether it is a directory or a regular file, can have extra information describing it: this infromatioons are called *metadata*.
Metadata informations are stored as a regular file, in JSON or YAML format, and they follow a strict naming convention.

The data stored as metadata are entirely defined by you, and as long as the metadata document's format is valid and matches the file extension, the system will be able to handle it.

## Format

By default, metadata files are assumed to be JSON files, however you can also use YAML format or a mix of both JSON and YAML. To change the default format, use the `metadataFormat` parameter in the configuration file.

This parameter accepts 3 values :
- **json**:  (default value) metadata file are JSON formatted
- **yaml** : metadata files are YAML formatted
- **mixed** : metadata files can be JSON or YAML formatted, depending on the file extension

If the *mixed* metadata format is configured, the system relies on the file extension to determine the actual format of a metadata file. 

> Although it is possible to mix JSON and YAML formats for metadata files, it is recommended to keep things simple, pick up a format and stick to it for all metadata files.

## Naming convention

The metadata file associated with an item must follow the naming convention described below : 

```
[related item's filename][".json" | ".yaml" | ".yml"].<metadata extension>
```

Where :
- **related item's filename** 
  - if the item is a *regular file*, this field is the filename **including its extension** 
  - if the item is a *directory* then this field remains blank.
- **[".json" | ".yaml" | ".yml"]**  defines the format of the metadata file. It is only required in *mixed* metadata format mode
- **\<metadata extension>** : metadata file extension. Default value is *meta*, but it can be modified via the `metadataExtension` configuration parameter. 

For example, the table below describe possible metadata filename for a the regular file **file.txt** depending on configuration.

 | `metadataExtension` | `metadataFormat` | actual format | metadata's filename    |
 | :------------------ | :--------------- | :------------ | :--------------------- |
 | *default*           | *default*        | json          | file.txt.meta          |
 | *default*           | yaml             | yaml          | file.txt.meta          |
 | *default*           | json             | json          | file.txt.meta          |
 | *default*           | mixed            | json          | file.txt.json.meta     |
 | *default*           | mixed            | yaml          | file.txt.yaml.meta     |
 | metainfo            | *default*        | json          | file.txt.metainfo      |
 | metainfo            | mixed            | json          | file.txt.json.metainfo |

In case the item is a directory, the string "file.txt" should simply be removed. For example :  ".json.meta", ".metainfo", etc.

## Location

Two possible cases:

- When the item is a regular file, its associated metadata file must be located in the same directory.
- When the item is a directory, the metadata file must be located in this directory.


