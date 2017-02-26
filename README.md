[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/bay-scalajs-g8/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# bay-scalajs.g8
This Template tries to eliminate all boilerplate and create some best-practices and guidelines to make web development in scala more straight forward.

For Api's it takes the approach to define the database scheme and the api-definition outside of this project in a different process, and helps you scaffold as much as possible into your server.

This way old tooling can be reused and modeling decisions are easier to discuss in a team environment. 

For example you can use [pg-modeler](http://www.pgmodeler.com.br/) to design your database and the official [swagger editor](http://editor.swagger.io) to define your api endpoints. After that you can scaffold both with the help of the code generators and only have to code the adapter to connect both.

## Dependencies / Install Instructions
To use this template you only have to install the java8 sdk and the newest version of sbt(0.13.13+). A local postgres database is also recommended.

> docker-compose will be available in the future

## Getting Started
cd into your workspace root, then type `sbt new daxten/bay-scalajs.g8` and follow the instructions. A new folder, named after your project, will be created.

## Available Commands
``sbt codegen``

Runs migrations and scaffolds the configured database into
* case classes inside the shared module
* slick schemas inside the dbdriver project

meta goodies:
* you can add methods to the `case class`es that get auto-generated, the codegen will preserve these changes

``sbt codegen-re``

similar to `sbt-codegen`, but removes the database first. This helps when changing the newest schema while it's not yet in source control / production. 

``sbt swagger``

Generate Swagger router traits / case classes from swagger files inside conf/swagger

> The codegen also works with multiple configured databases. It also tries to create a new database if it does not exist yet.

## Swagger

The Swagger codegen is not complete but supports a bunch of stuff already:
* generation of a typesafe version of  all paths 

> Only the "right" way is typesafe in this case, since there is a bunch of stuff which can go wrong, you can always return any result as an error. If you've defined these errors in swagger you will be able to use the generated case classes for these but  the error-side is never typesafe

>> In general, only json is supported as a serializer. I would like to auto-deserialize MultipartFormData into the right case class, but that is not supported atm. Though you can still use multipartformdata, it's just not as typesafe as the rest

* typesafe api_key-security usage via Header or Query

> more security support in the future

* grouping of routes into traits by tag
* all generated case classes can be changed, rerunning the swagger codegen will preserve any changes to the body
* multiple swagger configs in 1 project are supported

This is a swagger-first approach. In my experience it is easier to talk about swagger definitions in the team then about scala code. Also this way all swagger tooling can be reused.

## Used Libaries
You can find a complete list inside `project/Dependencies.scala`. I don't use any RC, M or pre-release libraries for this template.


## Structure
All Api calls using Autowire should use 

`type ApiResult[T] = Future[Either[ApiError, T]]` 

as the result type. You can take a look at `web/components/LoremIpsumComponent.scala` and `web/components/SimpleApiComponent.scala` on how to work with this type.

These components also show how to create generic components with scalajs-react, which isn't straightforward but eliminates some boilerplate, I've also added a `TestComponent` which logs its lifecycle to check if components inside these get reused (they do).

Application.scala also has an example how to use the monad-transformers inside the ExtendedController.

> shoutout to Erik Bakker, take a look at his talk: https://www.youtube.com/watch?v=hGMndafDcc8

```
def login: Action[AnyContent] = Action.async { implicit request =>
  val result = for {
    form <- loginForm.bindFromRequest()       |> HttpResult.fromForm(e => BadRequest(views.html.login(e)))
    userId <- userDao.maybeLogin(form)        |> HttpResult.fromFOption(BadRequest(views.html.login(loginForm.fill(form).withGlobalError("bad.password"))))
    loginResult <- gotoLoginSucceeded(userId) |> HttpResult.fromFuture
  } yield loginResult

  constructResult(result)
}
```

## Helper
`shared.utils.Implicits // usable as trait or import`

Put your usefull implicits in here, already contains an implicit class with extension methods to lift types to option/future, since I like suffixes more then wrapping them.


```
import shared.utils.Implicits._
val x: String = "Some String"
val o: Option[String] = x.asOption
val f: Future[Option[String]] = o.asFuture
```

> In my experience, you should not use implicit conversions.

`shared.utils.Codecs`

Contains Encoder/Decoder for uPickle and Circe. You will want to import / extend this when talking to the Api.

`utils.ReactTags`

Simply extend this trait to get vdom into scope.. the package name is way too long to remember for me.

## Why Circe AND uPickle?
uPickle works better for Client/Server communication using autowire (I had problems with Circe and sealed traits).

Circe seems to work better for creating external facing Api's.