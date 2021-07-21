Ham-App-Bot
---

# How to build with Gradle

* Set the following environment variables
  
  | Environment variable | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
  |----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
  | SLACK_BOT_TOKEN      | The valid bot token value starting with `xoxb-` in your development workspace. To issue a bot token, you need to install your Slack App that has a bot user to your development workspace. Visit the [Slack App configuration page](https://api.slack.com/apps/), choose the app you’re working on, and go to **Settings > Install App** on the left pane (Add `app_mentions:read` bot scope if you see the message saying "Please add at least one feature or permission scope to install your app."). |
  | SLACK_APP_TOKEN      | The valid app-level token value starting with `xapp-` for your Slack app. To issue an app-level token, visit the [Slack App configuration page](https://api.slack.com/apps/), choose the app you’re working on, and go to **Settings > Basic Information > App-Level Tokens**, and then create a new one with `connections:write` scope.                                                                                                                                                                |

* Run the bot

  ```
  > ./gradlew run
  ```

# How to build with Docker

* Build the Docker image

  ```
  > docker build -t IMAGE_NAME .
  ```

* Run the Docker image with the following environment variables

  | Environment variable | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
  |----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
  | SLACK_BOT_TOKEN      | The valid bot token value starting with `xoxb-` in your development workspace. To issue a bot token, you need to install your Slack App that has a bot user to your development workspace. Visit the [Slack App configuration page](https://api.slack.com/apps/), choose the app you’re working on, and go to **Settings > Install App** on the left pane (Add `app_mentions:read` bot scope if you see the message saying "Please add at least one feature or permission scope to install your app."). |
  | SLACK_APP_TOKEN      | The valid app-level token value starting with `xapp-` for your Slack app. To issue an app-level token, visit the [Slack App configuration page](https://api.slack.com/apps/), choose the app you’re working on, and go to **Settings > Basic Information > App-Level Tokens**, and then create a new one with `connections:write` scope.                                                                                                                                                                |

  ```
  > docker run -d -e SLACK_BOT_TOKEN='xoxb-...' -e SLACK_APP_TOKEN='xapp-...' IMAGE_NAME
  ```

# Helpful links

* [Slack App configuration page](https://api.slack.com/apps/)
* [Slack API Docs](https://api.slack.com/docs)

# TODO

1. add `help`-command
2. add `update`-command which pulls from repo and restarts the bot
