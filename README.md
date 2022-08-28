# Spring Rapid REST

[![Version](https://img.shields.io/badge/Version-v0.0.1-blue.svg?style=for-the-badge)](https://shields.io/)
[![Licence](https://img.shields.io/badge/Licence-MIT-success.svg?style=for-the-badge)](https://shields.io/)

This template enables the quick creation of a service that exposes REST endpoints for CRUD
operations over data persisted in a database. The developer needs to only define the entities in
the domain and their respective [repositories](https://docs.spring.io/spring-data/data-commons/docs/1.6.1.RELEASE/reference/html/repositories.html).
The template uses [ByteBuddy](https://bytebuddy.net/) to generate the required classes for the
endpoints to function.

## Features
- Automatic creation of supporting entities (history) to track the changes on the main entities
- Automatic conversion between history and main entities
- Configured endpoints for CRUD operations and viewing of history data
- Soft deletes of main entities

## Example

### Created Entity

### Created Repository

### Generated tables

### Generated Endpoints

## How to use

## Licence
[MIT](https://choosealicense.com/licenses/mit/)