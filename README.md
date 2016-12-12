# bay-scalajs.g8
This Template tries to eliminate all boilerplate and create some best-practices and guidelines to make web development in scala more straight forward.

## Dependencies / Install Instructions
To use this template you only have to install the java8 sdk and the newest version of sbt(0.13.13+). A local postgres database is also recommended.

> docker-compose will be available in the future

## Getting Started
cd into your workspace root, then type `sbt new daxten/bay-scalajs.g8` and follow the instructions. A new folder, named after your project, will be created.

cd into your project folder, then type `sbt codegen`. If you setuped everything correctly your database classes will be scaffolded into the project.