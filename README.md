# Slack-Bot

## Available environment variables

| Environment variable | Mandatory | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
|----------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SLACK_BOT_TOKEN      | yes       | The valid bot token value starting with `xoxb-` in your development workspace. To issue a bot token, you need to install your Slack App that has a bot user to your development workspace. Visit the [Slack App configuration page](https://api.slack.com/apps/), choose the app you’re working on, and go to **Settings > Install App** on the left pane (Add `app_mentions:read` bot scope if you see the message saying "Please add at least one feature or permission scope to install your app."). |
| SLACK_APP_TOKEN      | yes       | The valid app-level token value starting with `xapp-` for your Slack app. To issue an app-level token, visit the [Slack App configuration page](https://api.slack.com/apps/), choose the app you’re working on, and go to **Settings > Basic Information > App-Level Tokens**, and then create a new one with `connections:write` scope.                                                                                                                                                                |
| DATABASE_URL         | yes       | The URL to the MariaDB-instance (e.g. `localhost:3306/db`).                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| DATABASE_USER        | yes       | The username for the database.                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| DATABASE_PASSWORD    | yes       | The password for the database.                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| SLACK_BOT_ADMIN_IDS  | no        | The Slack member IDs separated by `,` (Example: `U012A345B67,U765B432A10`) responsible for managing the bot (will be shown extra admin-views in the home view). Invalid IDs or IDs that can't be found in the current workspace will be ignored.                                                                                                                                                                                                                                                        |
| DEBUG_MODE           | no        | If set to `true` the bot will work in debug mode and output more logs.                                                                                                                                                                                                                                                                                                                                                                                                                                  |

## How to build with Gradle

* Set the environment variables mentioned in `Available environment variables`
* Run the bot
  ```
  > ./gradlew run
  ```

## How to build with Docker

* Build the Docker image
  ```
  > docker build -t IMAGE_NAME .
  ```

* Run the Docker image with the environment variables mentioned in `Available environment variables`
  ```
  > docker run -d \
        -e SLACK_BOT_TOKEN='xoxb-...' \
        -e SLACK_APP_TOKEN='xapp-...' \
        -e DATABASE_URL='SOME_URL' \
        -e DATABASE_USER='SOME_USER' \
        -e DATABASE_PASSWORD='SOME_PASSWORD' \
        IMAGE_NAME
  ```

## Helpful links

* [Slack App configuration page](https://api.slack.com/apps/)
* [Slack API Docs](https://api.slack.com/docs)

## TODO

1. add section about how to configure the bot on [Slack App configuration page](https://api.slack.com/apps/)
2. add `help`-command
3. add `update`-command which pulls from repo and restarts the bot
