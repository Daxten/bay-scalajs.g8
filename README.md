> This Template is not 'officially' released yet, I use it for all of my projects and update it when I find time, I will make this release-ready in the future

# bay-scalajs.g8
This Template tries to eliminate all boilerplate and create some best-practices and guidelines to make web development in scala more straight forward.

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

``sbt codegen-re``

similar to `sbt-codegen`, but removes the database first. This helps when changing the newest schema while it's not yet in source control / production. 

> The codegen also works with multiple configured databases. It also tries to create a new database if it does not exist yet.

## Used Libaries
You can find a complete list inside `project/Dependencies.scala`. I don't use any RC, M or pre-release libraries for this template. 
`scala-java-time` is the only exception since there is no release for scalajs yet and it's working really well already (with the exception of timezones, which are not yet supported)

Talking about time, you will have to import `org.threeten.bp` for time, this will change in the future since `scala-java-time` already supports `java.time` imports.


## Structure
All Api calls using Autowire should use 

`type ApiResult[T] = Future[\/[ApiError, T]]` 

as the result type. You can take a look at `web/components/LoremIpsumComponent.scala` and `web/components/SimpleApiComponent.scala` on how to work with this type.

These components also show how to create generic components with scalajs-react, which isn't straightforward but eliminates some boilerplate, I've also added a `TestComponent` which logs its lifecycle to check if components inside these get reused (they do).

Application.scala also has an example how to use the monad-transformers inside the ExtendedController.

> shoutout to Erik Bakker, take a look at his talk: https://www.youtube.com/watch?v=hGMndafDcc8

```
def login: Action[AnyContent] = Action.async { implicit request =>
  val result = for {
    form <- loginForm.bindFromRequest() |> HttpResult.fromForm(e => BadRequest(views.html.login(e)))
    userId <- userDao.maybeLogin(form)  |> HttpResult.fromFOption(BadRequest(views.html.login(loginForm)))
  } yield gotoLoginSucceeded(userId)
  
  constructResultWithF(result)
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

Simply extend this trait to get vdom into scope. I've created since the package name is way too long to remember for me.

## Why Circe AND uPickle?
uPickle works better for Client/Server communication using autowire (I had problems with Circe and sealed traits).

Circe seems to work better for creating external facing Api's.