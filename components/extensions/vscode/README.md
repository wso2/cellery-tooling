# Cellery extension for Visual Studio Code

The Cellery VS Code extension makes it easy to write cell files by providing code suggestions and snippets.
This extension also provides deployment support for cellery with build, run and test commands.

## Prerequisites

Download and install relevant releases according to your system.

1. [Cellery](https://cellery.io/downloads)
2. [Ballerina](https://ballerina.io/downloads/): (1.0.3 is recommended)

Note: Ballerina VS Code extension automatically will be installed when installing the Cellery VS Code extension.

## Features

### Language support

Cellery is based on [Ballerina](https://ballerina.io/) and all the [Ballerina related tooling support](https://ballerina.io/learn/tools-ides/vscode-plugin/language-intelligence) is provided
when writing cell the files.

#### Cellery Specific Language Support

Users need to import `celleryio/cellery` module at the beginning of the cell file in order to have cellery specific
language support.

![import celleryio/cellery](resources/images/readme/import-cellery.png)

that's it! now you will have all the cellery related language support to write your cell file.
Following is a small demo which is showing writing a simple hello-world cell file with the help of VS Code plugin.

![write hello world cell](resources/images/readme/write-hello-world-cell.gif)

### Deployment support

VS Code extension for cellery help users to deploy their cells through VS Code commands. Open up the VS Code command
palette and type `cellery` to see available commands. You will see three commands in this extension.

#### Build Command:  

This will build a cell image using the cell file which VS Code is currently pointed. Users need to provide cell image
name as an input.

#### Run Command:  

User can use this command to build and run a cell file which he/she is currently pointing to. Cell image name and 
cell instance name are require as inputs.

#### Test Command:

Cell testing can be easily execute by using this command. Test command also require cell image name as an input.

![build hello world cell](resources/images/readme/build-cell-image.gif)

