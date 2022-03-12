# Daniel's Network Search

This is a public repo version of Daniel's Network Search.

This search will auto index, neocities.org, tilde.town and tilde.club

This project is split into 3 parts:

-   The backend handles searches and quiries sent from the frontend
-   The fontend is the public site
-   The indexer collects and processes site information

The backend service requires Node.js
The frontend service requires a web server such as Nginx
The indexer requires Java Runtime
A MySQL database is also required

## How to use this code

-   To setup, apply the database structure to your MySQL Database.
-   Build and run the indexer in java to populate the database.
-   Start the backend via Node.js (node main.js)
-   Setup a proxy passthrough to Node.js in your webserver if required.
-   Start the frontend via your webserver and connect!

You may need to alter files and paths as necessary for your server setup!
The database is intended to be connected to via an SSH tunnel to a local database instance.

Please note this code is unsupported at the moment and I dont plan to bug fix it, please use at your own risk!

If you launch a service using this code please link back to https://melonking.net :^)
