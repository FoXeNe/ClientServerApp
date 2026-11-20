# ClientServerApp

A Kotlin desktop app for managing a shared product collection. The JavaFX client shows products in a table and plots them by their coordinates. Everyone can browse the collection, but only the person who added a product can edit or delete it. The server stores products and user accounts in PostgreSQL.

## Run it locally

You'll need JDK 17 and Docker with Compose. The client runs on your desktop, outside Docker.

From the project root:

```sh
cp .env.example .env
```

Set `DB_USER` and `DB_PASSWORD` in `.env`. Keep `DB_NAME=studs` for the default Compose setup, since the server uses that name and Compose doesn't pass it a different one.

Start the server and database:

```sh
docker compose up -d --build
```

Then open the client:

```sh
bash gradlew :client:run
```

On Windows, use `gradlew.bat :client:run` instead. The client connects to `localhost:45205` by default. Set `SERVER_HOST` and `SERVER_PORT` before launching it if your server is running elsewhere.

Register an account from the login screen, then sign in to add your first product. The interface starts in Russian. You can also switch to Icelandic, Bulgarian, or Costa Rican Spanish.

## Project layout

- `client/` - JavaFX interface and TCP client
- `server/` - TCP server, collection commands, and PostgreSQL storage
- `common/` - models shared by the client and server
- `deploy/` - Ansible and Kubernetes deployment files
