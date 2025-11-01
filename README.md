
[![Documentation](https://i.imgur.com/7QDbrIS.png)](https://wiki.tomkeuper.com/docs/BedWarsProxy/) [![Report a Bug](https://i.imgur.com/Z1qOYLC.png)](https://github.com/tomkeuper/BWProxy2023/issues) [![API](https://i.imgur.com/JfMTMMc.png)](https://javadocs.tomkeuper.com/) [![Discord](https://i.imgur.com/yBySzkU.png)](https://discord.gg/kPaBGwhmjf)

**AstroidMC BedWars Proxy** is a plugin for Bungeecord networks that are running BedWars2023 in BUNGEE mode. This plugin provides features for lobby servers: join gui/ signs, placeholders and more.

![Signs](https://i.imgur.com/ggNRp4D.png?1)

**FEATURES**
- Dynamic game signs
- Static game signs
- Global arena selector
- Per group arena selector
- Rotating event gamemode GUI
- Arena rejoin system
- Admin /bw tp <player> command to catch cheaters
- Per player language system in sync with arenas
- PAPI placeholders
- Internal party system
- API for developers

**HOW TO USE**

All the information you need can be found on its [documentation / wiki](https://wiki.tomkeuper.com/docs/BedWarsProxy/).

**DOWNLOAD**
- [Latest release](#)
- [Development builds](https://github.com/tomkeuper/BWProxy2023/releases)

**API**

The API uses the package `com.astroid.bedwars.proxy.api` and provides extensive functionality for developers to integrate with the BedWars Proxy system.

**Maven Repository**

Add this repository to your `pom.xml`:
```xml
<repository>
    <id>astroidmc-releases</id>
    <url>https://maven.astroidmc.com/repository/maven-releases/</url>
</repository>
```

Add the dependency:
```xml
<dependency>
    <groupId>com.astroid.bedwars</groupId>
    <artifactId>proxy-api</artifactId>
    <version>1.0</version>
    <scope>provided</scope>
</dependency>
```

**For Developers/Contributors:**
- 📚 [MAVEN_DEPLOYMENT.md](MAVEN_DEPLOYMENT.md) - Full deployment guide
- ⚡ [DEPLOY.md](DEPLOY.md) - Quick reference commands

[![Discord](https://discordapp.com/api/guilds/760851292826107926/widget.png?style=shield)](https://discord.gg/kPaBGwhmjf) ![Servers](https://img.shields.io/bstats/servers/20358)