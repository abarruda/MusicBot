MusicBot
===========================

MusicBot is a automated bot that tracks music content detected in group messages on the [Telegram](https://telegram.org/) messaging platform.

## Features
- Tracks media content.
- Alerts group members to repeated media content.
- Provides a mobile interface to browse media content detected by groups' users and automatically play the content in its respective app!
- Goodies such as an auto-responder when user defined words/terms are detected.

### Supported Media Sources
- [SoundCloud](https://soundcloud.com)
- [YouTube](https://www.youtube.com)
- Mixcloud (coming soon)

## Getting Started
1. Add the existing bot I run `@TheMusicBot` to your Telegram [group](https://telegram.org/faq#q-what-makes-telegram-groups-cool), or if you have privacy concerns, follow the [Installation Instructions](#installation-instructions) to get the bot up and running.
2. Start sending videos and audio links to your group.
3. Message the bot directly with the command `/help` for instructions to start browsing your custom curated content!

## Installation Instructions
**MusicBot** relies on the [Docker](https://www.docker.com) container platform and [Docker Compose](https://docs.docker.com/compose/) tool.  They must be installed on a Linux system in order to run the MusicBot service properly.

1. Register your bot with the [BotFather](https://core.telegram.org/bots#6-botfather) and note the following:
  - API Token
  - Bot Username
2. Set [privacy mode](https://core.telegram.org/bots#privacy-mode) to **disabled**.
3. Clone this repository onto the system.
4. Set your database password: `export MONGODB_PASS=<password>`
5. Configure the API properties:
  1. rename `MusicBot/musicbot-api/api.template` to `MusicBot/musicbot-api/api.properties`
  2. edit `api.properties` and fill in the above password for the `db.user.password` property.
6. Configure the Bot properties:
  1. rename `MusicBot/musicbot-processor/processor.template` to `MusicBot/musicbot-processor/processor.properties`
  2. edit `processor.properties` and fill in the following properpties:
    - `api.token` : The API token provided to you by the [BotFather](https://core.telegram.org/bots#6-botfather).
    - `bot.name` : The username of the bot.
    - `website` : The URI of the host for the bot's mobile UI (include a trailing slash).
    - `db.user.password` : The database password you set in step 3.
7. Run `docker-compose build` to build the docker images that comproise the **MusicBot** service.
8. Run `docker-compose up` to start the bot's services.

#### Technologies
- Java
- [MongoDB](https://www.mongodb.com/)
- Google [Guice](https://github.com/google/guice), [Guava](https://github.com/google/guava)
- [Metrics](http://metrics.dropwizard.io/)
- [Docker](https://www.docker.com)
- [Docker Compose](https://docs.docker.com/compose/)
- [Telegram Bot API](https://core.telegram.org/bots)