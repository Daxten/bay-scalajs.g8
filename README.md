> This Template is not 'officially' released yet, I use it for all of my projects and update it when I find time, I will make this release-ready in the future

# bay-scalajs.g8
This Template tries to eliminate all boilerplate and create some best-practices and guidelines to make web development in scala more straight forward.

## Dependencies / Install Instructions
To use this template you only have to install the java8 sdk and the newest version of sbt(0.13.13+). A local postgres database is also recommended.

> docker-compose will be available in the future

## Getting Started
cd into your workspace root, then type `sbt new daxten/bay-scalajs.g8` and follow the instructions. A new folder, named after your project, will be created.

cd into your project folder, then type `sbt codegen`. If you setuped everything correctly your database classes will be scaffolded into the project.

## ReactStores
ReactStore is a new library under-development which will be extracted into it's own project hopefully. It tries to eliminate boilerplate when creating flux-like stores and handle their state. 

```scala
// A Simple Collection

// Model, must live inside the shared modul
case class Contact(key: String, name: String, telephone: String)

// Use autowire (see example in Api.scala) to wire client/server

// Client Side
object Store {
    val contacts = new ReactStore[String, Contact] {
        override val name: String = "Contact Store"
        override def getId(e: ContactStack): String = e.key
        override def init: Future[Seq[ContactStack]] = AjaxClient[Api].getContacts().call()

        // Add methods to change the store
        def update(contact: Contact): Callback = Callback.future {
          AjaxClient[Api].updateContact(contact).call() map {
            case \/-(updatedContact) =>
              updateOrInsertIntoStore(updatedContact)
            case _ => 
              Callback.empty
          }
        }
    }
}

// You also get a Component to Render the Store, it will update on changes automatically
Store.contacts.render(
    error => <.span(error.getMessage),   // Throwable Error
    pending => <.span(s"pending for $pending ms"),
    <.div("Store is Ready and Empty"),  // lazy
    contacts => {
        <.table(
            <.tbody(
                contacts.map { contact =>
                    <.tr(
                        <.td(contact.key),
                        <.td(contact.name)
                    )
                }
            )
        )
    }
)
```