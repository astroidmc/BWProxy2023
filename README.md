
[![Documentation](https://i.imgur.com/7QDbrIS.png)](https://wiki.tomkeuper.com/docs/BedWarsProxy/) [![Report a Bug](https://i.imgur.com/Z1qOYLC.png)](https://github.com/tomkeuper/BWProxy2023/issues) [![API](https://i.imgur.com/JfMTMMc.png)](https://javadocs.tomkeuper.com/) [![Discord](https://i.imgur.com/yBySzkU.png)](https://discord.gg/kPaBGwhmjf)

**BWProxy2023** is a plugin for Bungeecord networks that are running BedWars2023 in BUNGEE mode. This plugin provides features for lobby servers: join gui/ signs, placeholders and more.

![Signs](https://i.imgur.com/ggNRp4D.png?1)

**FEATURES**
- Dynamic game signs
- Static game signs
- Global arena selector
- Per group arena selector
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

**MAVEN REPO (AstroidMC)**
```xml
<repository>
    <id>astroidmc-releases</id>
    <url>https://maven.astroidmc.com/repository/maven-releases/</url>
</repository>

<!--Use for Snapshots only!-->
<repository>
    <id>astroidmc-snapshots</id>
    <url>https://maven.astroidmc.com/repository/maven-snapshots/</url>
</repository>
<!-- -->

<dependency>
    <groupId>com.tomkeuper.bedwars</groupId>
    <artifactId>proxy-api</artifactId>
    <version>{version}</version>
    <scope>provided</scope>
</dependency>
```

**DEPLOYING TO MAVEN REPOSITORY**

To deploy the API to the AstroidMC Maven repository, follow these steps:

📚 **Full deployment guide:** See [MAVEN_DEPLOYMENT.md](MAVEN_DEPLOYMENT.md)  
⚡ **Quick reference:** See [DEPLOY.md](DEPLOY.md)  
📝 **Settings template:** See [settings.xml.template](settings.xml.template)

**Quick Start:**

1. **Configure Maven settings** (`~/.m2/settings.xml`):
```xml
<settings>
    <servers>
        <server>
            <id>astroidmc-releases</id>
            <username>your-username</username>
            <password>your-password</password>
        </server>
        <server>
            <id>astroidmc-snapshots</id>
            <username>your-username</username>
            <password>your-password</password>
        </server>
    </servers>
</settings>
```

2. **Deploy release version** (proxy-api only):
```bash
cd proxy-api
mvn clean deploy
```

Or from project root:
```bash
mvn clean deploy -pl proxy-api
```

3. **Deploy snapshot version**:
Update version in `pom.xml` to include `-SNAPSHOT` suffix, then:
```bash
mvn clean deploy -pl proxy-api
```

4. **Full project build** (includes plugin):
```bash
mvn clean package
```

**Note:** Only the `proxy-api` module should be deployed to Maven. The `proxy-plugin` module is distributed as a JAR file.

**Maven Repository Access:**
- Web UI: https://maven.astroidmc.com/
- Releases: https://maven.astroidmc.com/#browse/browse:maven-releases
- Snapshots: https://maven.astroidmc.com/#browse/browse:maven-snapshots

[![Discord](https://discordapp.com/api/guilds/760851292826107926/widget.png?style=shield)](https://discord.gg/kPaBGwhmjf) ![Servers](https://img.shields.io/bstats/servers/20358)