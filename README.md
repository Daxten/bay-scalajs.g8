# BayTemplate-ScalaJs
A ScalaJs Template with scaffolding, postgres, scalajs-react and play-framework in the backend

## Opinionated
This Template is opinionated and uses Postgres as the database engine.

## Codegen
To start a codegeneration task for all databases defined inside your application.conf run:
```
$ sbt "project codegen" run
```
> (TODO: This Template is not auto generating models on compile and puts the models into the vcs, this should not change in my opinion, add text about this here and/or change it if someone has some good arguments about this)
